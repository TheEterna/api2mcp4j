#!/usr/bin/env bash
#
# publish-to-central.sh
#
# server2mcp · 发布到 Sonatype Central Portal（Maven 中央仓库）
#
# 流程：
#   1. 跑 scripts/pre-release-check.sh（凭据类按 --require-credentials 判定）
#   2. --dry-run ：mvn -Prelease clean verify   —— 本地全量验证（sources + javadoc），不上传、不签名
#   3. 真实发布 ：mvn -Prelease clean deploy   —— GPG 签名 + 上传 Central Portal
#
# 前置条件：
#   - ~/.m2/settings.xml 含 id=central 的 server（参考 settings.xml.example）
#   - 环境变量 CENTRAL_PORTAL_TOKEN 已设置（或 settings.xml 内直接写 token）
#   - 本机 gpg 已有私钥，且公钥已上传 keyserver.ubuntu.com
#
# Usage:
#   ./scripts/publish-to-central.sh --dry-run
#   ./scripts/publish-to-central.sh --publish
#
# Exit codes:
#   0 = 发布/验证成功
#   1 = 前置检查或发布失败
#
# @author han
# @time 2026/10/27

set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${REPO_ROOT}"

MODE="${1:-}"
case "${MODE}" in
  --dry-run)
    REQUIRE_CRED=0
    MVN_GOAL="clean verify"
    ;;
  --publish)
    REQUIRE_CRED=1
    MVN_GOAL="clean deploy"
    ;;
  *)
    cat <<EOF
用法：
  $0 --dry-run    本地验证（产出 sources + javadoc，不签名、不上传）
  $0 --publish    真实发布（GPG 签名 + 上传 Sonatype Central Portal）
EOF
    exit 1
    ;;
esac

echo "=============================================="
echo " server2mcp · Maven Central 发布"
echo " 模式：${MODE} · 目标：${MVN_GOAL}"
echo "=============================================="

# ---------------------------------------------------------------- 前置检查
CHECK_ARGS=""
[ "${REQUIRE_CRED}" = "1" ] && CHECK_ARGS="--require-credentials"

if ! bash scripts/pre-release-check.sh ${CHECK_ARGS}; then
  echo ""
  echo "❌ 前置检查未通过，发布中止。"
  exit 1
fi

# ---------------------------------------------------------------- dry-run 额外产物核验
if [ "${MODE}" = "--dry-run" ]; then
  echo ""
  echo "▶ 执行本地验证构建：mvn -Prelease ${MVN_GOAL}"
  if ! mvn -Prelease ${MVN_GOAL}; then
    echo ""
    echo "❌ 验证构建失败。"
    exit 1
  fi

  echo ""
  echo "▶ 核验发布件清单"
  MISSING=0
  for module in \
    server2mcp-common \
    server2mcp-core \
    server2mcp-autoconfigure \
    server2mcp-spring-boot-starters/server2mcp-starter-webmvc \
    server2mcp-spring-boot-starters/server2mcp-starter-webflux; do
    ART_DIR="${module}/target"
    VER=$(mvn -q -N help:evaluate -Dexpression=project.version -DforceStdout 2>/dev/null | tail -1)
    for suffix in ".jar" "-sources.jar" "-javadoc.jar"; do
      ART="${ART_DIR}/$(basename "${module}")-${VER}${suffix}"
      if [ -f "${ART}" ]; then
        printf '  \033[32m✓\033[0m %s\n' "${ART}"
      else
        printf '  \033[31m✗\033[0m %s （缺失）\n' "${ART}"
        MISSING=1
      fi
    done
  done

  if [ "${MISSING}" = "1" ]; then
    echo ""
    echo "❌ 发布件缺失，dry-run 判定失败。"
    exit 1
  fi

  echo ""
  echo "✅ dry-run 通过：全部 sources + javadoc 已就绪，尚未签名与上传。"
  exit 0
fi

# ---------------------------------------------------------------- 真实发布
echo ""
echo "▶ 确认：即将把 $(mvn -q -N help:evaluate -Dexpression=project.version -DforceStdout 2>/dev/null | tail -1) 上传到 Sonatype Central Portal"
echo "  （Central 发布后不可撤回、永久公开索引）"
read -r -p "  输入 PUBLISH 确认继续：" CONFIRM
if [ "${CONFIRM}" != "PUBLISH" ]; then
  echo "已取消。"
  exit 1
fi

# GPG passphrase：从 ~/.m2/gpg-passphrase（权限 600）注入环境变量，不落 pom、不入仓
if [ -z "${GPG_PASSPHRASE:-}" ] && [ -f "${HOME}/.m2/gpg-passphrase" ]; then
  GPG_PASSPHRASE="$(cat "${HOME}/.m2/gpg-passphrase")"
  export GPG_PASSPHRASE
  echo "  已从 ~/.m2/gpg-passphrase 注入 GPG_PASSPHRASE"
fi

echo ""
echo "▶ 执行发布：mvn -Prelease ${MVN_GOAL}"
if ! mvn -Prelease ${MVN_GOAL}; then
  echo ""
  echo "❌ 发布失败。"
  exit 1
fi

echo ""
echo "✅ 上传完成。请到 Central Portal 查看校验状态（Validating → Published）。"
exit 0
