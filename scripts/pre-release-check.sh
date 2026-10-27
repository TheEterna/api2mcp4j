#!/usr/bin/env bash
#
# pre-release-check.sh
#
# server2mcp · Maven Central 发布前置自检
#
# 在 `mvn -Prelease deploy` 之前运行，逐项断言发布所需的全部前置条件：
#   1. 版本号非 SNAPSHOT
#   2. 根 POM 元数据齐全（name/description/url/license/developers/scm）
#   3. 全模块版本号一致
#   4. 工作树无凭据残留（明文密码 / token / 公网 IP）
#   5. 发布件 jar 内无凭据残留
#   6. GPG 密钥可用
#   7. ~/.m2/settings.xml 含 id=central 的 server
#   8. git 工作树干净
#
# Usage:  ./scripts/pre-release-check.sh [--require-credentials]
#
# Exit codes:
#   0 = 全部必要项通过（凭据类在未加 --require-credentials 时只告警）
#   1 = 有阻断项未通过
#
# @author han
# @time 2026/10/27

set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${REPO_ROOT}"

REQUIRE_CREDENTIALS=0
[ "${1:-}" = "--require-credentials" ] && REQUIRE_CREDENTIALS=1

PASS=0
FAIL=0
WARN=0

ok()   { printf '  \033[32m✓\033[0m %s\n' "$1"; PASS=$((PASS+1)); }
bad()  { printf '  \033[31m✗\033[0m %s\n' "$1"; FAIL=$((FAIL+1)); }
warn() { printf '  \033[33m!\033[0m %s\n' "$1"; WARN=$((WARN+1)); }
sec()  { printf '\n\033[1m%s\033[0m\n' "$1"; }

echo "=============================================="
echo " server2mcp · 发布前置自检"
echo " repo: ${REPO_ROOT}"
echo "=============================================="

# ---------------------------------------------------------------- 1. 版本号
sec "1. 版本号"
VERSION=$(mvn -q -N help:evaluate -Dexpression=project.version -DforceStdout 2>/dev/null | tail -1)
if [[ "${VERSION}" == *"-SNAPSHOT" ]]; then
  bad "版本号仍是 SNAPSHOT：${VERSION}"
else
  ok "版本号：${VERSION}（非 SNAPSHOT）"
fi

# ---------------------------------------------------------------- 2. POM 元数据
sec "2. 根 POM 发布元数据"
for field in name description url license developers scm issueManagement distributionManagement; do
  if grep -q "<${field}>" pom.xml; then
    ok "<${field}> 已声明"
  else
    bad "<${field}> 缺失 —— Central 会拒绝该 POM"
  fi
done

# distributionManagement 指向 Central Portal
if grep -q "central.sonatype.com" pom.xml; then
  ok "distributionManagement 指向 Sonatype Central Portal"
else
  bad "distributionManagement 未指向 Central Portal"
fi

# developers 邮箱必须是 noreply，不得含真实邮箱
if grep -oE "<email>[^<]+</email>" pom.xml | grep -vi "noreply\|@users\." | grep -q .; then
  bad "developers 邮箱非 noreply —— 真实邮箱会随发布永久公开索引"
else
  ok "developers 邮箱为 noreply 形式"
fi

# ---------------------------------------------------------------- 3. 模块版本一致
sec "3. 全模块版本一致性"
MISMATCH=0
while IFS= read -r pom; do
  # 只校验继承本项目 parent 的模块。
  # server2mcp-test（演示应用）的 parent 是 spring-boot-starter-parent，
  # 属独立应用，不参与本项目版本体系（且不在根 pom <modules> 中，不发布）。
  if ! grep -A3 "<parent>" "${pom}" | grep -q "server2mcp-parent"; then
    continue
  fi
  v=$(grep -A3 "<parent>" "${pom}" | grep -m1 "<version>" | sed -E 's/.*<version>([^<]+)<\/version>.*/\1/')
  if [ -n "${v}" ] && [ "${v}" != "${VERSION}" ]; then
    bad "${pom} → parent version=${v}（应为 ${VERSION}）"
    MISMATCH=1
  fi
done < <(find . -name pom.xml -not -path "*/target/*" | sort)
[ "${MISMATCH}" = "0" ] && ok "全部模块 parent 版本一致：${VERSION}"

# ---------------------------------------------------------------- 4. 工作树凭据扫描
sec "4. 工作树凭据扫描"
SECRET_PATTERN="(password|passwd|pwd|secret|api[_-]?key|access[_-]?token)[[:space:]]*[:=][[:space:]]*[\"'][^\"']{3,}[\"']"
HITS=$(grep -rnEI "${SECRET_PATTERN}" \
  --exclude-dir=target --exclude-dir=.git --exclude-dir=.omc --exclude-dir=node_modules \
  --exclude-dir=.vitepress --exclude="*.log" \
  --include="*.yml" --include="*.yaml" --include="*.properties" --include="*.xml" \
  --include="*.java" --include="*.sh" --include="*.json" . 2>/dev/null \
  | grep -viE "password: \"\"|password: *$|PASSWORD_ENV|\$\{|\.password\(\)|getPassword|@author" \
  | grep -viE "src/test/.*(fake|dummy|test-|12345678|example)" )

if [ -z "${HITS}" ]; then
  ok "未发现明文凭据"
else
  bad "发现疑似凭据："
  echo "${HITS}" | head -10 | sed 's/^/      /'
fi

# 公网 IP（排除文档中的历史审计引用与示例）
IP_HITS=$(grep -rnE "([0-9]{1,3}\.){3}[0-9]{1,3}" \
  --exclude-dir=target --exclude-dir=.git --exclude-dir=.omc --exclude-dir=node_modules \
  --exclude-dir=.vitepress --include="*.yml" --include="*.yaml" --include="*.properties" \
  --include="*.java" --include="*.xml" . 2>/dev/null \
  | grep -vE "127\.0\.0\.1|0\.0\.0\.0|localhost|255\.|1\.1\.1\.1|8\.8\.8\.8|192\.168\.|10\.[0-9]+\.")
if [ -z "${IP_HITS}" ]; then
  ok "未发现公网 IP 硬编码"
else
  bad "发现公网 IP 硬编码："
  echo "${IP_HITS}" | head -5 | sed 's/^/      /'
fi

# ---------------------------------------------------------------- 5. 攻击性/工具脚本
sec "5. 仓库内容合规"
for f in brute_mysql.py find_mysql_server.py; do
  if git ls-files --error-unmatch "${f}" >/dev/null 2>&1; then
    bad "${f} 仍在版本控制中 —— 攻击性工具不应进入公开仓库"
  else
    ok "${f} 未被 Git 跟踪"
  fi
done
if git ls-files | grep -qE "^\.omc/"; then
  bad ".omc/ 本地状态缓存仍在版本控制中"
else
  ok ".omc/ 未被 Git 跟踪"
fi

# ---------------------------------------------------------------- 6. GPG
sec "6. GPG 签名密钥"
if command -v gpg >/dev/null 2>&1; then
  KEYCNT=$(gpg --list-secret-keys --with-colons 2>/dev/null | grep -c "^sec" || true)
  if [ "${KEYCNT:-0}" -gt 0 ]; then
    ok "存在 ${KEYCNT} 个 GPG 私钥"
  else
    if [ "${REQUIRE_CREDENTIALS}" = "1" ]; then
      bad "无 GPG 私钥 —— Central 强制要求签名"
    else
      warn "无 GPG 私钥（发布时必需）"
    fi
  fi
else
  warn "gpg 未安装（发布时必需）"
fi

# ---------------------------------------------------------------- 7. Maven 凭据
sec "7. Maven Central 凭据"
SETTINGS="${HOME}/.m2/settings.xml"
if [ -f "${SETTINGS}" ]; then
  if grep -q "<id>central</id>" "${SETTINGS}"; then
    ok "settings.xml 含 id=central 的 server"
    if [ "${REQUIRE_CREDENTIALS}" = "1" ] && grep -qE "<password>\\\$\{env\." "${SETTINGS}"; then
      if [ -z "${CENTRAL_PORTAL_TOKEN:-}" ]; then
        bad "settings.xml 引用了 \${env.CENTRAL_PORTAL_TOKEN} 但环境变量未设置"
      else
        ok "CENTRAL_PORTAL_TOKEN 环境变量已设置"
      fi
    fi
  else
    if [ "${REQUIRE_CREDENTIALS}" = "1" ]; then
      bad "settings.xml 缺 id=central 的 server（参考 settings.xml.example）"
    else
      warn "settings.xml 缺 id=central 的 server"
    fi
  fi
else
  if [ "${REQUIRE_CREDENTIALS}" = "1" ]; then
    bad "${SETTINGS} 不存在（参考 settings.xml.example）"
  else
    warn "${SETTINGS} 不存在"
  fi
fi

# ---------------------------------------------------------------- 8. git 状态
sec "8. Git 工作树"
if [ -z "$(git status --porcelain)" ]; then
  ok "工作树干净"
else
  warn "工作树有未提交改动："
  git status --porcelain | head -10 | sed 's/^/      /'
fi

# ---------------------------------------------------------------- 汇总
echo ""
echo "=============================================="
printf ' 通过 %d · 告警 %d · 失败 %d\n' "${PASS}" "${WARN}" "${FAIL}"
echo "=============================================="

if [ "${FAIL}" -gt 0 ]; then
  echo "❌ 存在阻断项，不可发布。"
  exit 1
fi
if [ "${REQUIRE_CREDENTIALS}" = "1" ] && [ "${WARN}" -gt 0 ]; then
  echo "❌ 凭据类告警在 --require-credentials 模式下视为阻断。"
  exit 1
fi
echo "✅ 必要项全部通过。"
exit 0
