#!/usr/bin/env node
/**
 * @header-start
 * @module check-file-header
 * @keywords 文件头审计 AI可读性 FILE_HEADER_SPEC @header-start @header-end @module
 *           @keywords @updated @layer @purpose @affects 校验器 lint 门禁 退出码
 *           依赖流向 层级一致性 规范落地 可验证
 * @updated 2026-10-28 12:30
 * @purpose 机器断言全仓 .java 文件头合规，把「新增文件必须有文件头」从人工抽查变成可复跑门禁
 * @layer common
 * @see docs/specs/FILE_HEADER_SPEC.md — 文件头规范正本
 * @see docs/plan-文件头回填Sprint-2026-10-28.md — 本脚本的落地计划
 * @constraint 规范层工具：只读不改；判定标准以 FILE_HEADER_SPEC.md 为唯一权威源，改判定须同步改规范
 * @author han
 * @header-end
 */

import { readdirSync, readFileSync, statSync } from 'node:fs';
import { join, relative } from 'node:path';

const ROOT = process.cwd();

/** 规范 §六 五层枚举 —— 唯一权威取值 */
const VALID_LAYERS = new Set(['common', 'core', 'autoconfigure', 'starter', 'test']);

/** 模块目录 → @layer 期望值（依赖流向铁律：common ← core ← autoconfigure ← starter） */
const MODULE_LAYER = [
  { seg: 'server2mcp-common', layer: 'common' },
  { seg: 'server2mcp-core', layer: 'core' },
  { seg: 'server2mcp-autoconfigure', layer: 'autoconfigure' },
  { seg: 'server2mcp-starter-webmvc', layer: 'starter' },
  { seg: 'server2mcp-starter-webflux', layer: 'starter' },
  { seg: 'server2mcp-spring-boot-starters', layer: 'starter' },
  { seg: 'server2mcp-test', layer: 'test' },
];

const REQUIRED_FIELDS = ['@module', '@keywords', '@updated', '@layer'];
const UPDATED_RE = /@updated\s+(\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2})/;

function walk(dir, acc = []) {
  let entries;
  try {
    entries = readdirSync(dir, { withFileTypes: true });
  } catch {
    return acc;
  }
  for (const e of entries) {
    if (e.name === 'target' || e.name === '.git' || e.name === 'node_modules') continue;
    const full = join(dir, e.name);
    if (e.isDirectory()) walk(full, acc);
    else if (e.name.endsWith('.java')) acc.push(full);
  }
  return acc;
}

function expectedLayer(relPath) {
  if (relPath.includes('/src/test/')) return 'test';
  for (const { seg, layer } of MODULE_LAYER) {
    if (relPath.includes(`${seg}/`)) return layer;
  }
  return null;
}

const files = walk(ROOT);
const violations = [];

for (const abs of files) {
  const rel = relative(ROOT, abs);
  const src = readFileSync(abs, 'utf8');
  const problems = [];

  const start = src.indexOf('@header-start');
  const end = src.indexOf('@header-end');
  if (start === -1) problems.push('缺少 @header-start');
  if (end === -1) problems.push('缺少 @header-end');
  if (start !== -1 && end !== -1 && end < start) problems.push('@header-end 出现在 @header-start 之前');

  const header = start !== -1 && end !== -1 ? src.slice(start, end) : '';

  for (const f of REQUIRED_FIELDS) {
    if (!new RegExp(`\\${f}\\s+\\S`).test(header)) problems.push(`缺必填字段 ${f}`);
  }

  const kw = header.match(/@keywords\s+(.*)/);
  if (kw && kw[1].trim().length < 10) problems.push('@keywords 过短（<10 字符，占位敷衍）');

  const layer = header.match(/@layer\s+(\S+)/);
  if (layer) {
    const v = layer[1];
    if (!VALID_LAYERS.has(v)) problems.push(`@layer 取值非法：「${v}」不在 {common|core|autoconfigure|starter|test}`);
    const exp = expectedLayer(rel);
    if (exp && v !== exp) problems.push(`@layer 与所在模块不一致：期望「${exp}」实为「${v}」`);
  }

  const upd = header.match(UPDATED_RE);
  if (!upd) problems.push('@updated 格式非法（须 YYYY-MM-DD HH:mm）');

  if (problems.length) violations.push({ file: rel, problems });
}

const total = files.length;
const bad = violations.length;

console.log(`文件头审计：扫描 ${total} 个 .java 文件`);
if (bad === 0) {
  console.log('✅ 全部合规（@header-start/@header-end 齐备 · 必填字段齐备 · @layer 合法且与模块一致 · @updated 格式正确）');
  process.exit(0);
}

console.log(`❌ ${bad} 个文件不合规：\n`);
for (const v of violations) {
  console.log(`  ${v.file}`);
  for (const p of v.problems) console.log(`    - ${p}`);
}
console.log(`\n规范正本：docs/specs/FILE_HEADER_SPEC.md`);
process.exit(1);
