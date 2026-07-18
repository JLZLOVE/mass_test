好，VO 已建好，我只写 \*\*Controller 方法签名\*\* 和 \*\*Service 接口方法签名\*\*，不写实现。所有逻辑用注释说明。



\---



\## 一、PortalController 方法签名



```java

@RestController

@RequestMapping("/portal")

@IgnoreAuth

@Slf4j

public class PortalController {



&#x20;   private final NoticeInfoService noticeInfoService;

&#x20;   private final ActivityApplyService activityApplyService;

&#x20;   private final SysClubService sysClubService;



&#x20;   public PortalController(NoticeInfoService noticeInfoService,

&#x20;                           ActivityApplyService activityApplyService,

&#x20;                           SysClubService sysClubService) {

&#x20;       this.noticeInfoService = noticeInfoService;

&#x20;       this.activityApplyService = activityApplyService;

&#x20;       this.sysClubService = sysClubService;

&#x20;   }



&#x20;   /\*\*

&#x20;    \* 通知列表（分页）

&#x20;    \*/

&#x20;   @GetMapping("/notices")

&#x20;   public R<Page<PortalNoticeListVO>> listNotices(

&#x20;           @RequestParam(defaultValue = "1") Integer page,

&#x20;           @RequestParam(defaultValue = "10") Integer size) {

&#x20;       // Service 层逻辑：

&#x20;       // 1. 仅查 receiver\_type=1（全体学生）且 status=1（已发布）

&#x20;       // 2. 排序 top\_flag DESC, publish\_time DESC

&#x20;       // 3. 批量查 readCount（先查 id 列表，再 IN 查 notice\_read\_record）

&#x20;       // 4. summary 从 content 截取（逻辑见下方说明）

&#x20;       return R.ok(noticeInfoService.portalList(page, size));

&#x20;   }



&#x20;   /\*\*

&#x20;    \* 通知详情

&#x20;    \*/

&#x20;   @GetMapping("/notices/{noticeNo}")

&#x20;   public R<PortalNoticeDetailVO> getNoticeDetail(

&#x20;           @PathVariable String noticeNo) {

&#x20;       // Service 层逻辑：

&#x20;       // 1. 根据 noticeNo 查通知，不存在抛 6401

&#x20;       // 2. viewCount + 1（异步更新，不影响响应）

&#x20;       // 3. readCount / receiverCount / readRate 统统计

&#x20;       // 4. 附件列表：仅当 attachment\_min\_level 为 null 或 0 时返回

&#x20;       return R.ok(noticeInfoService.portalDetail(noticeNo));

&#x20;   }



&#x20;   /\*\*

&#x20;    \* 活动列表（分页）

&#x20;    \*/

&#x20;   @GetMapping("/activities")

&#x20;   public R<Page<PortalActivityListVO>> listActivities(
&#x20;           @RequestParam(defaultValue = "1") Integer page,
&#x20;           @RequestParam(defaultValue = "10") Integer size,
&#x20;           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime freezeTime) {

&#x20;       // Service 层逻辑：

&#x20;       // 1. freezeTime 不传则取当前时间
&#x20;       // 2. 若 freezeTime > 当前时间 + 5 分钟，视为无效，回退为当前时间
&#x20;       // 3. 时间范围：start\_time >= freezeTime - 6个月 AND start\_time <= freezeTime + 2个月

&#x20;       // 4. 仅查 approve\_status = 4（已通过）

&#x20;       // 5. 排序 start\_time ASC

&#x20;       // 6. 联查 sys\_club 取 club\_name 和 category

&#x20;       // 7. 联查 activity\_category 取 category\_name

&#x20;       // 8. 列表不返回 organizerNote

&#x20;       return R.ok(activityApplyService.portalList(page, size, freezeTime));

&#x20;   }



&#x20;   /\*\*

&#x20;    \* 活动详情

&#x20;    \*/

&#x20;   @GetMapping("/activities/{activityNo}")

&#x20;   public R<PortalActivityDetailVO> getActivityDetail(

&#x20;           @PathVariable String activityNo) {

&#x20;       // Service 层逻辑：

&#x20;       // 1. 防篡改校验：从 activityNo 提取日期时间（substring(2,14)），与 create\_time 截断到分钟比对

&#x20;       // 2. 不一致 → 封锁（approve\_status=8）+ 通知管理员 + 抛 7319

&#x20;       // 3. 查活动详情，含签到配置（左连 activity\_sign\_config）

&#x20;       // 4. 仅返回公开字段：不含 GPS 坐标（signLatitude/Longitude/Radius）

&#x20;       // 5. signAvailable 运行时计算

&#x20;       return R.ok(activityApplyService.portalDetail(activityNo));

&#x20;   }



&#x20;   /\*\*

&#x20;    \* 社团列表（全量返回，不分页）

&#x20;    \*/

&#x20;   @GetMapping("/clubs")

&#x20;   public R<List<PortalClubVO>> listClubs(

&#x20;           @RequestParam(required = false) String category) {

&#x20;       // Service 层逻辑：

&#x20;       // 1. 仅查 sys\_club.status = 1（正常运营）

&#x20;       // 2. 可选按 category 筛选

&#x20;       // 3. 排序 category ASC, create\_time DESC

&#x20;       // 4. 联查 sys\_college 取 college\_name

&#x20;       // 5. category 存储的是枚举编码，返回时需映射为中文名

&#x20;       return R.ok(sysClubService.portalList(category));

&#x20;   }

}

```



\---



\## 二、Service 接口新增方法签名



\### 2.1 NoticeInfoService



```java

public interface NoticeInfoService extends IService<NoticeInfo> {



&#x20;   // 现有方法...（保持不变）



&#x20;   /\*\*

&#x20;    \* 门户通知列表（分页）

&#x20;    \* 仅查 receiver\_type=1 且 status=1

&#x20;    \* 批量查 readCount：先查 id 列表，再用 IN 查 notice\_read\_record

&#x20;    \* summary 截取逻辑：

&#x20;    \*   - 去除 HTML 标签

&#x20;    \*   - 有效字符 < 50 → 展示原标题（不截取）

&#x20;    \*   - 50 ≤ 有效字符 ≤ 150 → 展示前 20%（不含标点符号）

&#x20;    \*   - 有效字符 > 150 → 展示前 150 字符（不含标点符号）

&#x20;    \*/

&#x20;   Page<PortalNoticeListVO> portalList(int page, int size);



&#x20;   /\*\*

&#x20;    \* 门户通知详情

&#x20;    \* 不存在抛 6401

&#x20;    \* viewCount + 1（异步更新）

&#x20;    \* 返回 readCount / receiverCount / readRate

&#x20;    \* 附件：仅 attachment\_min\_level 为 null 或 0 时返回

&#x20;    \*/

&#x20;   PortalNoticeDetailVO portalDetail(String noticeNo);

}

```



\### 2.2 ActivityApplyService



```java

public interface ActivityApplyService extends IService<ActivityApply> {



&#x20;   // 现有方法...（保持不变）



&#x20;   /\*\*

&#x20;    \* 门户活动列表（分页）
&#x20;    \* 仅查 approve\_status = 4
&#x20;    \* freezeTime 不传则取当前时间；若超过 「当前时间 + 5 分钟」则回退为当前时间
&#x20;    \* 时间范围：freezeTime - 6个月 ～ freezeTime + 2个月

&#x20;    \* 联查 sys\_club 取 club\_name、category（category 枚举→中文名）

&#x20;    \* 联查 activity\_category 取 category\_name

&#x20;    \* 排序 start\_time ASC

&#x20;    \* 不返回 organizerNote

&#x20;    \* 不返回 viewCount（表里没有）

&#x20;    \*/

&#x20;   Page<PortalActivityListVO> portalList(int page, int size, LocalDateTime freezeTime);



&#x20;   /\*\*

&#x20;    \* 门户活动详情

&#x20;    \* 防篡改校验：activityNo 日期与 create\_time 比对，不一致时：

&#x20;    \*   - 更新 approve\_status = 8（STATUS\_BLOCKED）

&#x20;    \*   - 发系统通知（院级→dean\_id，校级→SUPER\_ADMIN）

&#x20;    \*   - 抛 7319

&#x20;    \* 联查 activity\_sign\_config 取签到配置（仅公开字段：时间窗口、方式、签退开关）

&#x20;    \* 不返回 GPS 坐标（signLatitude、signLongitude、signRadius）

&#x20;    \* signAvailable 运行时计算

&#x20;    \*/

&#x20;   PortalActivityDetailVO portalDetail(String activityNo);

}

```



\### 2.3 SysClubService



```java

public interface SysClubService extends IService<SysClub> {



&#x20;   // 现有方法...（保持不变）



&#x20;   /\*\*

&#x20;    \* 门户社团列表（全量返回，不分页）

&#x20;    \* 仅查 status = 1（正常运营）

&#x20;    \* 可选按 category 筛选

&#x20;    \* 排序 category ASC, create\_time DESC

&#x20;    \* 联查 sys\_college 取 college\_name

&#x20;    \* category 枚举码→中文名映射

&#x20;    \*/

&#x20;   List<PortalClubVO> portalList(String category);

}

```



\---



\## 三、逻辑汇总说明（给开发看）



| 模块 | 方法 | 关键逻辑 | 表/依赖 |

| :--- | :--- | :--- | :--- |

| 通知 | `portalList` | 批量查 readCount（IN 查询） | `notice\_info`、`notice\_read\_record` |

| 通知 | `portalDetail` | viewCount+1（异步），readRate 计算 | `notice\_info`、`notice\_read\_record` |

| 活动 | `portalList` | 时间冻结，联查社团+分类 | `activity\_apply`、`sys\_club`、`activity\_category` |

| 活动 | `portalDetail` | 防篡改→封锁→通知管理员 | `activity\_apply`、`activity\_sign\_config` |

| 社团 | `portalList` | 枚举码映射中文名 | `sys\_club`、`sys\_college` |



\---



\## 四、待实现细节（设计阶段定，不等代码）



\### 4.1 批量查 readCount 的 SQL 结构



```sql

\-- 列表查询时先取 id 列表

SELECT id, notice\_no, title, content, publish\_time, top\_flag, cover\_image, attachment\_min\_level, receiver\_count, view\_count

FROM notice\_info

WHERE status = 1 AND receiver\_type = 1 AND publish\_time <= NOW()

ORDER BY top\_flag DESC, publish\_time DESC;



\-- 批量查 readCount

SELECT notice\_id, COUNT(\*) AS read\_count

FROM notice\_read\_record

WHERE notice\_id IN (id列表) AND is\_read = 1

GROUP BY notice\_id;

```



\### 4.2 summary 截取辅助方法



放在 `StringUtils` 或新建 `TextSummaryUtil`：

\- 输入：`String htmlContent`

\- 输出：`String summary`

\- 步骤：Jsoup 去除 HTML 标签 → 去除标点符号 → 按有效字符数截取



\### 4.3 活动编号防篡改 + 封锁事务

**统一编号格式：** `{2位缩写前缀}{yyyyMMddHHmm}{5位随机}`（19位），例 `WH20260718143082739`
在 `portalDetail` 中：
1\. 解析 `activityNo.substring(2, 14)` 提取时间（yyyyMMddHHmm）

2\. 与 `create\_time` 截断到分钟比对

3\. 不一致 → `@Transactional` 内执行：

&#x20;  - `activity.setApproveStatus(STATUS\_BLOCKED)` + `updateById`

&#x20;  - `noticeAutoPublisher.publishBlockedNotice(activity)`

&#x20;  - 抛 `EIException(ErrorConfig.ACT_CODE_TAMPER)`



\---



以上是全部方法签名 + 逻辑说明。\*\*没有任何实现代码\*\*。可以过一遍看看有没有遗漏的方法参数或返回值。

