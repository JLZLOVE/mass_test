package untiy.security;

import org.springframework.util.StringUtils;
import untiy.entity.SysMenu;
import untiy.entity.vo.MenuTreeVO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 内存组装菜单树：纯静态方法，一次遍历建索引再挂接，禁止递归查库。
 */
public final class MenuTreeHelper {

    private static final int MENU_TYPE_DIRECTORY = 1;
    private static final int MENU_TYPE_PAGE = 2;

    private MenuTreeHelper() {
    }

    public static List<MenuTreeVO> buildTree(List<SysMenu> menus, long rootParentId) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, MenuTreeVO> nodeMap = new HashMap<>();
        for (SysMenu menu : menus) {
            if (menu.getMenuType() != null && menu.getMenuType() == 3) {
                continue;
            }
            nodeMap.put(menu.getId(), toNode(menu));
        }

        List<MenuTreeVO> roots = new ArrayList<>();
        for (MenuTreeVO node : nodeMap.values()) {
            Long parentId = normalizeParentId(node.getParentId());
            if (parentId == rootParentId) {
                roots.add(node);
            } else {
                MenuTreeVO parent = nodeMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(node);
                }
            }
        }

        sortRecursive(roots);
        pruneEmptyDirectories(roots);
        return roots;
    }

    /**
     * 移除没有可见子节点（页面或仍有子目录）的空目录。
     */
    public static void pruneEmptyDirectories(List<MenuTreeVO> nodes) {
        if (nodes == null) {
            return;
        }
        Iterator<MenuTreeVO> it = nodes.iterator();
        while (it.hasNext()) {
            MenuTreeVO node = it.next();
            if (node.getMenuType() != null && node.getMenuType() == MENU_TYPE_DIRECTORY) {
                pruneEmptyDirectories(node.getChildren());
                if (!hasVisibleContent(node)) {
                    it.remove();
                }
            }
        }
    }

    private static boolean hasVisibleContent(MenuTreeVO directory) {
        if (directory.getChildren() == null || directory.getChildren().isEmpty()) {
            return false;
        }
        for (MenuTreeVO child : directory.getChildren()) {
            if (child.getMenuType() != null && child.getMenuType() == MENU_TYPE_PAGE) {
                return true;
            }
            if (child.getMenuType() != null && child.getMenuType() == MENU_TYPE_DIRECTORY && hasVisibleContent(child)) {
                return true;
            }
        }
        return false;
    }

    private static void sortRecursive(List<MenuTreeVO> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        nodes.sort(Comparator
                .comparing(MenuTreeVO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MenuTreeVO::getId, Comparator.nullsLast(Long::compareTo)));
        for (MenuTreeVO node : nodes) {
            sortRecursive(node.getChildren());
        }
    }

    private static MenuTreeVO toNode(SysMenu menu) {
        MenuTreeVO vo = new MenuTreeVO();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setMenuName(menu.getMenuName());
        vo.setMenuType(menu.getMenuType());
        vo.setPermissionCode(menu.getPermissionCode());
        vo.setComponentPath(menu.getComponentPath());
        vo.setRoutePath(menu.getRoutePath());
        vo.setIcon(menu.getIcon());
        vo.setSort(menu.getSort());
        vo.setStatus(menu.getStatus());
        return vo;
    }

    public static long normalizeParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }

    /**
     * 从已授权菜单向上补全祖先节点 ID。
     */
    public static List<SysMenu> expandWithAncestors(List<SysMenu> assigned, Map<Long, SysMenu> allById) {
        if (assigned == null || assigned.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, SysMenu> merged = assigned.stream()
                .collect(Collectors.toMap(SysMenu::getId, m -> m, (a, b) -> a));
        for (SysMenu menu : assigned) {
            Long parentId = menu.getParentId();
            while (parentId != null && parentId != 0) {
                SysMenu parent = allById.get(parentId);
                if (parent == null) {
                    break;
                }
                merged.putIfAbsent(parent.getId(), parent);
                parentId = parent.getParentId();
            }
        }
        return new ArrayList<>(merged.values());
    }

    /**
     * 收集所有非空 permission_code。
     */
    public static List<String> collectPermissionCodes(List<SysMenu> menus) {
        if (menus == null) {
            return new ArrayList<>();
        }
        return menus.stream()
                .map(SysMenu::getPermissionCode)
                .filter(StringUtils::hasText)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
