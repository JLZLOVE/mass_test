const fs = require('fs');
const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  AlignmentType, BorderStyle, WidthType, ShadingType
} = require('docx');

const FONT = { ascii: "Arial", hAnsi: "Arial", eastAsia: "Microsoft YaHei" };
const border = { style: BorderStyle.SINGLE, size: 1, color: "999999" };
const borders = { top: border, bottom: border, left: border, right: border };
const cm = { top: 60, bottom: 60, left: 100, right: 100 };

function hCell(text, w) {
  return new TableCell({
    borders, width: { size: w, type: WidthType.DXA },
    shading: { fill: "F0F0F0", type: ShadingType.CLEAR }, margins: cm,
    children: [new Paragraph({ children: [new TextRun({ text, bold: true, font: FONT, size: 20 })] })]
  });
}
function bCell(text, w) {
  return new TableCell({
    borders, width: { size: w, type: WidthType.DXA }, margins: cm,
    children: [new Paragraph({ children: [new TextRun({ text, font: FONT, size: 20 })] })]
  });
}

function sec(text) {
  return new Paragraph({ spacing: { before: 240, after: 100 },
    children: [new TextRun({ text, bold: true, font: FONT, size: 24, color: "1A1A2E" })] });
}
function sub(text) {
  return new Paragraph({ spacing: { after: 60 },
    children: [new TextRun({ text, font: FONT, size: 18, color: "666666" })] });
}
function p(text) {
  return new Paragraph({ spacing: { after: 60, line: 300 },
    children: [new TextRun({ text, font: FONT, size: 20 })] });
}
function bp(prefix, text) {
  return new Paragraph({ spacing: { after: 30, line: 300 }, indent: { left: 360 },
    children: [
      new TextRun({ text: "\u00B7 ", font: FONT, size: 20, color: "333333" }),
      new TextRun({ text: prefix, font: FONT, size: 20, bold: true }),
      new TextRun({ text, font: FONT, size: 20 })
    ]
  });
}
function line() {
  return new Paragraph({ spacing: { before: 40, after: 40 },
    border: { bottom: { style: BorderStyle.SINGLE, size: 1, color: "DDDDDD" } }, children: [] });
}

const doc = new Document({
  styles: { default: { document: { run: { font: FONT, size: 20 } } } },
  sections: [{
    properties: { page: { size: { width: 11906, height: 16838 }, margin: { top: 1000, right: 1100, bottom: 1000, left: 1100 } } },
    children: [

      // 姓名
      new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 30 },
        children: [new TextRun({ text: "冯依林", bold: true, font: FONT, size: 36, color: "1A1A2E" })] }),
      // 联系方式
      new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 20 },
        children: [
          new TextRun({ text: "13253564316", font: FONT, size: 19, color: "555555" }),
          new TextRun({ text: "  |  ", font: FONT, size: 19, color: "CCCCCC" }),
          new TextRun({ text: "jlz2fyl@qq.com", font: FONT, size: 19, color: "555555" }),
          new TextRun({ text: "  |  ", font: FONT, size: 19, color: "CCCCCC" }),
          new TextRun({ text: "github.com/JLZLOVE/mass_test", font: FONT, size: 19, color: "555555" })
        ]
      }),
      // 求职意向
      new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 20 },
        children: [new TextRun({ text: "求职意向：Java开发实习生  |  每周可实习5天，可立即到岗", font: FONT, size: 20, bold: true })] }),

      line(),

      sec("教育经历"),
      new Paragraph({ spacing: { after: 60 },
        children: [
          new TextRun({ text: "2023.09 - 2027.06", font: FONT, size: 20, color: "666666" }),
          new TextRun({ text: "  |  ", font: FONT, size: 20, color: "CCCCCC" }),
          new TextRun({ text: "中原工学院", font: FONT, size: 20, bold: true }),
          new TextRun({ text: "  |  软件工程专业  |  本科", font: FONT, size: 20 })
        ]
      }),
      p("核心课程：Java程序设计、数据结构、数据库系统原理、操作系统、计算机网络、软件测试"),

      line(),

      sec("技术栈"),
      new Table({
        width: { size: 100, type: WidthType.PERCENTAGE }, columnWidths: [2200, 6806],
        rows: [
          new TableRow({ cantSplit: true, children: [
            hCell("后端", 2200),
            bCell("Java、SpringBoot、MyBatis-Plus、SpringSecurity、JWT、Maven", 6806)
          ]}),
          new TableRow({ cantSplit: true, children: [
            hCell("数据库", 2200),
            bCell("MySQL、Redis", 6806)
          ]}),
          new TableRow({ cantSplit: true, children: [
            hCell("前端（了解）", 2200),
            bCell("HTML、JavaScript、Vue3", 6806)
          ]}),
          new TableRow({ cantSplit: true, children: [
            hCell("工具", 2200),
            bCell("Git、IntelliJ IDEA、Postman、Swagger/Knife4j、Docker（了解）", 6806)
          ]}),
        ]
      }),

      line(),

      sec("项目经历"),
      new Paragraph({ spacing: { after: 30 },
        children: [
          new TextRun({ text: "mass_test 社团综合管理平台", font: FONT, size: 21, bold: true }),
          new TextRun({ text: "  |  独立开发", font: FONT, size: 20, color: "666666" })
        ]
      }),
      sub("2026.02 - 2026.04  |  SpringBoot + MyBatis-Plus + Redis + Vue3"),

      p("基于 SpringBoot 的前后端分离系统，实现社团管理、活动审批、通知发布、签到管理等核心功能，独立完成需求分析、数据库设计、编码开发、测试交付的全流程。"),

      bp("权限体系：", "设计五级 RBAC 权限体系（超管→管理员→社长→部长→学生），实现三层分离：准入鉴权（自定义注解 + AOP 接口级拦截）、数据行过滤（按社团数据孤岛隔离）、字段脱敏（不同级别可见字段不同）。"),

      bp("审批流程：", "设计五级审批链（部长→社长→指导老师→学院书记→校书记），实现动态审批人分配和超时自动转交；引入乐观锁防止并发审批冲突，编号校验机制防止数据篡改。"),

      bp("数据库设计：", "设计 21 张数据表，实现 20+ 个 RESTful API 接口；对高频字段建立索引优化查询性能；使用 Redis 缓存热点数据，提升系统响应速度。"),

      bp("代码规范：", "设计统一错误码体系（9 个区间，覆盖用户、活动、菜单、社团、审批等模块），集成 Swagger/Knife4j 自动生成 API 文档，通过 Git 进行版本管理。"),

      line(),

      sec("校园经历"),
      sub("2024.09 - 2025.06  |  场务管理协会负责人"),
      p("负责校园安全活动的组织协调，20 人团队支撑全校上百名学生，积累了团队协作和沟通能力。"),

      line(),

      sec("自我评价"),
      p("软件工程专业在读，Java 基础扎实，有独立完成完整项目的经验。学习能力强，对新技术有强烈的求知欲；具备良好的沟通能力和团队合作精神，能快速融入团队并独立承担模块开发任务。"),

    ]
  }]
});

Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync('D:\\冯\\简历\\冯依林_Java开发实习生_简历.docx', buffer);
  console.log('Done!');
});