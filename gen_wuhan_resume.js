const fs = require("fs");
const { Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell, BorderStyle, WidthType, ShadingType, LevelFormat, AlignmentType } = require("docx");

const font = { ascii: "Arial", hAnsi: "Arial", eastAsia: "Microsoft YaHei" };
const thinBorder = { style: BorderStyle.SINGLE, size: 1, color: "D1D5DB" };
const cellBorders = { top: thinBorder, bottom: thinBorder, left: thinBorder, right: thinBorder };
const cellMargins = { top: 60, bottom: 60, left: 120, right: 120 };

function cell(text, width, opts = {}) {
  return new TableCell({
    borders: cellBorders,
    width: { size: width, type: WidthType.DXA },
    margins: cellMargins,
    children: [
      new Paragraph({
        spacing: { before: 0, after: 0 },
        children: [new TextRun({ text, font, size: 20, bold: !!opts.bold })]
      })
    ]
  });
}

function heading(text) {
  return new Paragraph({
    spacing: { before: 240, after: 100 },
    children: [new TextRun({ text, font, size: 26, bold: true })]
  });
}

function normal(text) {
  return new Paragraph({
    spacing: { before: 0, after: 80 },
    children: [new TextRun({ text, font, size: 22 })]
  });
}

function small(text) {
  return new Paragraph({
    spacing: { before: 0, after: 60 },
    children: [new TextRun({ text, font, size: 20, color: "4B5563" })]
  });
}

function bullet(text) {
  return new Paragraph({
    spacing: { before: 0, after: 50 },
    indent: { left: 360, hanging: 180 },
    children: [new TextRun({ text: "\u2022 " + text, font, size: 20 })]
  });
}

function divider() {
  return new Paragraph({
    spacing: { before: 80, after: 80 },
    border: { bottom: { style: BorderStyle.SINGLE, size: 1, color: "D1D5DB", space: 1 } },
    children: []
  });
}

// ============ Build Document ============
const children = [];

// Header
children.push(
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 0, after: 0 },
    children: [new TextRun({ text: "冯依林", font, size: 40, bold: true })]
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 50, after: 80 },
    children: [
      new TextRun({ text: "13253564316  |  jlz2fyl@qq.com  |  github.com/JLZLOVE/mass_test", font, size: 20, color: "4B5563" })
    ]
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 0, after: 0 },
    children: [
      new TextRun({ text: "求职意向：Java开发实习生  |  每周可实习5天，可立即到岗", font, size: 20, bold: true })
    ]
  }),
  divider()
);

// Education
children.push(
  heading("教育经历"),
  new Paragraph({
    spacing: { before: 0, after: 40 },
    children: [
      new TextRun({ text: "2023.09 - 2027.06", font, size: 20, bold: true }),
      new TextRun({ text: "  |  ", font, size: 20, color: "9CA3AF" }),
      new TextRun({ text: "中原工学院", font, size: 20, bold: true }),
      new TextRun({ text: "  |  软件工程专业  |  本科", font, size: 20 })
    ]
  }),
  new Paragraph({
    spacing: { before: 0, after: 0 },
    children: [
      new TextRun({ text: "核心课程：Java程序设计、数据结构、数据库系统原理、操作系统、计算机网络、软件测试", font, size: 20, color: "4B5563" })
    ]
  }),
  divider()
);

// Tech Stack
children.push(heading("技术栈"));

const techTable = new Table({
  width: { size: 100, type: WidthType.PERCENTAGE },
  columnWidths: [1500, 8006],
  rows: [
    new TableRow({
      cantSplit: true,
      children: [
        cell("后端", 1500, { bold: true }),
        cell("Java、SpringBoot（IoC/DI、AOP）、MyBatis-Plus（拦截器机制）、SpringSecurity、JWT、Maven", 8006)
      ]
    }),
    new TableRow({
      cantSplit: true,
      children: [
        cell("数据库", 1500, { bold: true }),
        cell("MySQL（索引优化、SQL调优）、Redis（缓存策略）", 8006)
      ]
    }),
    new TableRow({
      cantSplit: true,
      children: [
        cell("前端", 1500, { bold: true }),
        cell("HTML、JavaScript、Vue3（了解）", 8006)
      ]
    }),
    new TableRow({
      cantSplit: true,
      children: [
        cell("运维", 1500, { bold: true }),
        cell("Linux基本操作、Docker容器化部署", 8006)
      ]
    }),
    new TableRow({
      cantSplit: true,
      children: [
        cell("工具", 1500, { bold: true }),
        cell("Git、IntelliJ IDEA、Postman、Swagger/Knife4j", 8006)
      ]
    })
  ]
});

children.push(techTable);
children.push(divider());

// Project Experience
children.push(
  heading("项目经历"),
  new Paragraph({
    spacing: { before: 0, after: 40 },
    children: [
      new TextRun({ text: "mass_test 社团综合管理平台", font, size: 22, bold: true }),
      new TextRun({ text: "  |  独立开发", font, size: 20, color: "4B5563" })
    ]
  }),
  new Paragraph({
    spacing: { before: 0, after: 80 },
    children: [
      new TextRun({ text: "2026.02 - 2026.04  |  SpringBoot + MyBatis-Plus + Redis + Vue3", font, size: 20, color: "4B5563" })
    ]
  }),
  small("独立完成从需求分析到上线交付的全流程开发（2个月），系统涵盖社团管理、活动审批、通知发布、签到管理等核心功能。"),
  bullet("后端开发：基于SpringBoot的IoC/DI容器管理组件依赖，通过AOP实现5级RBAC权限校验与操作日志统一拦截；利用MyBatis-Plus拦截器机制实现活动编号自动生成与数据防篡改校验。"),
  bullet("数据库设计：设计21张数据表，对高频查询字段建立索引；使用Redis缓存热点数据，优化接口响应速度。"),
  bullet("代码规范：设计统一错误码体系（覆盖9个模块范围段），集成Swagger/Knife4j自动生成API文档，遵循RESTful API设计规范，通过Git进行版本管理。"),
  bullet("部署运维：在Linux环境下通过Docker部署应用，具备基本的服务器操作和日志排查能力。"),
  divider()
);

// Campus
children.push(
  heading("校园经历"),
  new Paragraph({
    spacing: { before: 0, after: 40 },
    children: [
      new TextRun({ text: "2024.09 - 2025.06", font, size: 20, bold: true }),
      new TextRun({ text: "  |  ", font, size: 20, color: "9CA3AF" }),
      new TextRun({ text: "场务管理协会负责人", font, size: 20, bold: true })
    ]
  }),
  small("负责校园安全活动的组织协调，20人团队支撑全校上百名学生，积累了团队协作和沟通能力。"),
  divider()
);

// Self Evaluation
children.push(
  heading("自我评价"),
  small("软件工程专业在读，Java基础扎实，有独立完成完整项目的经验。熟悉SpringBoot框架核心原理（IoC/DI、AOP），了解MyBatis-Plus拦截器机制，具备良好的面向对象编程思维和代码规范意识。学习能力强，对新技术有强烈的求知欲；具备良好的沟通能力和团队合作精神，能快速融入团队并独立承担模块开发任务。")
);

const doc = new Document({
  sections: [{
    properties: {
      page: {
        size: { width: 11906, height: 16838 },
        margin: { top: 1200, right: 1200, bottom: 1000, left: 1200 }
      }
    },
    children
  }]
});

const outPath = "D:\\冯\\简历\\冯依林_Java开发实习生_武汉.docx";
Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync(outPath, buffer);
  console.log("Done: " + outPath);
}).catch(err => console.error(err));