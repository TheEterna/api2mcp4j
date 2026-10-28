import { defineConfig } from 'vitepress'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const HERE = path.dirname(fileURLToPath(import.meta.url))
const SITE_ROOT = path.resolve(HERE, '..')

/**
 * Build a nav list from a directory of Markdown files, newest first.
 *
 * Work logs and plans used to be listed by hand — which meant every new
 * document had to be added manually, and the site silently went stale
 * (2026-08-04 → 2026-10-28: seven logs and three plans never appeared).
 * Generating them from the filesystem removes that second source of truth.
 *
 * @param {string} dir   absolute directory to scan
 * @param {string} base  URL prefix for the generated links
 */
function buildNavFrom(dir, base) {
  if (!fs.existsSync(dir)) return []
  return fs
    .readdirSync(dir)
    .filter((f) => f.endsWith('.md'))
    .map((f) => {
      const slug = f.replace(/\.md$/, '')
      const parts = slug.split('_')
      const date = /^\d{4}-\d{2}-\d{2}$/.test(parts[0]) ? parts[0] : ''
      const topic = parts.length > 2 ? parts.slice(2).join('_') : slug
      return { text: date ? `${date} — ${topic}` : topic, link: `${base}/${slug}` }
    })
    .sort((a, b) => b.text.localeCompare(a.text))
}

/**
 * VitePress config for the api2mcp4j documentation site.
 *
 * Source: Markdown files from the framework repository (../README.md,
 * ../README_zh.md, ../docs/, ../docs/logs/, etc.) copied into this
 * directory by `scripts/build-docs.sh`. Build output:
 * `.vitepress/dist/` (Cloudflare Pages serves directly).
 */
export default defineConfig({
  title: 'api2mcp4j',
  description: 'Zero-to-low code MCP integration for Spring Boot REST APIs',
  lang: 'en-US',
  lastUpdated: true,
  cleanUrls: true,
  appearance: 'dark',
  srcDir: '.',

  // Markdown-it config — disable inline HTML parsing so that prose like
  // "<compilerArgs>" or "<plugin>" (common in tech docs) does not get
  // treated as an unclosed Vue template tag.
  markdown: {
    html: false,
    // 技术文档里大量出现 GitHub Actions 的 `${{ secrets.* }}` 语法。
    // VitePress 把 Markdown 渲染结果当作 Vue 模板编译，Vue 模板编译器会扫描
    // 文本节点里的 '{{' 并尝试当作插值表达式求值 → "Error parsing JavaScript expression"
    // → 整站构建失败（2026-10-28 首次触发：同步进 08-05 隐私审计日志后）。
    //
    // 1.6.4 没有关闭 Vue 插值的官方开关（插值规则挂在 core 的 'text' 上，无独立规则名）。
    // 本项目文档站不需要任何 Vue 插值，故给行内代码加 Vue 的 `v-pre` 指令 ——
    // 它让该元素内的插值解析整体跳过，代码内容原样呈现。
    config: (md) => {
      md.renderer.rules.code_inline = (tokens, idx) => {
        const token = tokens[idx];
        return `<code v-pre>${md.utils.escapeHtml(token.content)}</code>`;
      };
    },
  },

  // The mirrored Markdown sources contain many relative links to
  // repository files (./LICENSE, server2mcp-core/..., etc.) that don't
  // resolve inside the docs site — they exist for GitHub rendering.
  // Disable the dead-link check so the build succeeds; the GitHub-side
  // README remains the canonical source for those links.
  ignoreDeadLinks: true,

  head: [
    ['meta', { name: 'theme-color', content: '#1f2937' }],
    ['meta', { property: 'og:title', content: 'api2mcp4j — Spring Boot to MCP' }],
    ['meta', { property: 'og:description', content: 'Turn existing @RestController beans into MCP tools. 100% protocol 2026-07-28 compatible.' }],
    ['meta', { property: 'og:type', content: 'website' }],
  ],

  themeConfig: {
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Quick Start', link: '/quick-start' },
      {
        text: 'Protocol 2026-07-28',
        link: '/mcp-2026-07-28-INTEGRATION-MATRIX',
      },
      {
        text: 'Docs',
        items: [
          { text: 'Architecture', link: '/reference/architecture' },
          { text: 'Extension Points', link: '/reference/extension-points' },
          { text: 'Onboarding', link: '/reference/onboarding' },
        ],
      },
      {
        text: 'Specs',
        items: [
          { text: 'Registration Discipline', link: '/specs/REGISTRATION_DISCIPLINE_SPEC' },
          { text: 'Test Spec', link: '/specs/TEST_SPEC' },
          { text: 'File Header Spec', link: '/specs/FILE_HEADER_SPEC' },
          { text: 'Work Log Spec', link: '/specs/WORK_LOG_SPEC' },
        ],
      },
      {
        text: 'Work Logs',
        items: buildNavFrom(path.join(SITE_ROOT, 'logs'), '/logs'),
      },
      {
        text: 'Plans',
        items: buildNavFrom(path.join(SITE_ROOT, 'docs'), '/docs').filter(
          (item) => item.link.includes('/plan-'),
        ),
      },
      {
        text: '🔗',
        items: [
          { text: 'GitHub', link: 'https://github.com/TheEterna/api2mcp4j' },
          { text: '中文 README', link: '/zh-cn' },
        ],
      },
    ],

    sidebar: {
      '/': [
        {
          text: 'Introduction',
          items: [
            { text: 'What is api2mcp4j?', link: '/' },
            { text: 'Quick Start', link: '/quick-start' },
            { text: 'Why api2mcp4j?', link: '/why' },
          ],
        },
        {
          text: 'Protocol 2026-07-28',
          items: [
            { text: 'Integration Matrix', link: '/mcp-2026-07-28-INTEGRATION-MATRIX' },
            { text: 'One-line Verification', link: '/verify' },
            { text: 'Coverage Index', link: '/mcp-2026-07-28-coverage' },
            { text: 'Impact Analysis', link: '/mcp-2026-07-28-impact' },
          ],
        },
        {
          text: 'Architecture',
          items: [
            { text: 'Overview', link: '/reference/architecture' },
            { text: 'Extension Points', link: '/reference/extension-points' },
            { text: 'Onboarding', link: '/reference/onboarding' },
          ],
        },
        {
          text: 'Specs',
          items: [
            { text: 'Registration Discipline', link: '/specs/REGISTRATION_DISCIPLINE_SPEC' },
            { text: 'Test Spec', link: '/specs/TEST_SPEC' },
            { text: 'File Header Spec', link: '/specs/FILE_HEADER_SPEC' },
            { text: 'Work Log Spec', link: '/specs/WORK_LOG_SPEC' },
          ],
        },
        {
          text: 'Global Rules',
          items: [
            { text: 'Search Tool Parity', link: '/rules/global/search-tool-parity' },
            { text: 'Destructive Deletion', link: '/rules/global/destructive-deletion' },
            { text: 'Session Continuity', link: '/rules/global/session-continuity' },
            { text: 'Refactor Ordering', link: '/rules/global/refactor-ordering' },
            { text: 'Agent Capability', link: '/rules/global/agent-capability-declaration' },
            { text: 'Work Log', link: '/rules/global/work-log' },
          ],
        },
        {
          text: 'Work Logs',
          items: [
            { text: '2026-08-03 — Protocol 100% Compat', link: '/logs/2026-08-03_ceo_protocol-2026-07-28-100pct-compat' },
            { text: '2026-08-03 — Demo E2E 21/21', link: '/logs/2026-08-03_ceo_demo-end-to-end-21of21' },
            { text: '2026-08-03 — MRTR Wrapper + Safety', link: '/logs/2026-08-03_ceo_mrtr-callback-wrapper-safety-limits' },
            { text: '2026-06-24 — Heritage Battle Closure', link: '/logs/2026-06-24_CEO_继承心法战役闭环简报' },
          ],
        },
      ],
      '/zh-cn': [
        {
          text: '介绍',
          items: [
            { text: '什么是 api2mcp4j？', link: '/zh-cn' },
            { text: '快速开始', link: '/zh-cn/quick-start' },
            { text: '为什么选 api2mcp4j？', link: '/zh-cn/why' },
          ],
        },
        {
          text: '协议 2026-07-28',
          items: [
            { text: '集成矩阵', link: '/mcp-2026-07-28-INTEGRATION-MATRIX' },
            { text: '一行验证', link: '/verify' },
          ],
        },
        {
          text: '架构',
          items: [
            { text: '总览', link: '/reference/architecture' },
            { text: '扩展点', link: '/reference/extension-points' },
            { text: '3 步入门', link: '/reference/onboarding' },
          ],
        },
      ],
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/TheEterna/api2mcp4j' },
    ],

    footer: {
      message: 'Apache 2.0 Licensed · 100% Protocol 2026-07-28 Compatible',
      copyright: `Copyright © 2026-present Han`,
    },

    editLink: {
      pattern: 'https://github.com/TheEterna/api2mcp4j/edit/master/docs-site/:path',
      text: 'Edit this page on GitHub',
    },

    search: {
      provider: 'local',
    },
  },
})