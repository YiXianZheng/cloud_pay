package com.cloud.sysconf.common.utils;

import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.SysMenuDto;
import com.cloud.sysconf.common.dto.SysOfficeDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Auther Toney
 * @Date 2018/7/26 20:05
 * @Description:
 */
public class BuildTree {

    /**
     * 创建菜单树形结构
     * @param nodes
     * @param <T>
     * @return
     */
    public static <T> SysMenuDto build(List<SysMenuDto> nodes) {

        if(nodes == null){
            return null;
        }
        List<SysMenuDto> topNodes = new ArrayList<SysMenuDto>();

        for (SysMenuDto children : nodes) {

            String pid = children.getParentId();
            if (HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(pid) || HeaderInfoDto.AUTH_AGENT_SYSTEM.equals(pid)
                    || HeaderInfoDto.AUTH_MERCHANT_SYSTEM.equals(pid)) {
                topNodes.add(children);
                continue;
            }

            for (SysMenuDto parent : nodes) {
                String id = parent.getId();
                if (id != null && id.equals(pid)) {
                    parent.getChildren().add(children);

                    continue;
                }
            }
        }

        SysMenuDto root = new SysMenuDto();
        root.setId("000000");
        root.setParentId("");
        root.setChildren(topNodes);
        root.setName("我的菜单");

        return root;
    }

    /**
     * 创建组织树形结构
     * @param nodes
     * @return
     */
    public static List<SysOfficeDto> buildOffice(List<SysOfficeDto> nodes) {

        if(nodes == null){
            return null;
        }
        List<SysOfficeDto> res = new ArrayList<>();

        for (SysOfficeDto children : nodes) {

            children.setKey(children.getId());
            String pid = children.getParentId();
            if ("0".equals(pid)) {
                res.add(children);
                continue;
            }

            for (SysOfficeDto parent : nodes) {
                String id = parent.getId();
                if (id != null && id.equals(pid)) {

                    parent.getChildren().add(children);

                    continue;
                }
            }
        }

        return res;
    }

    /**
     * 创建菜单树形结构  显示系统
     * @param nodes
     * @return
     */
    public static List<SysMenuDto> buildMenu(List<SysMenuDto> nodes) {

        if(nodes == null){
            return null;
        }
        List<SysMenuDto> res = new ArrayList<>();

        for (SysMenuDto children : nodes) {

            children.setKey(children.getId());
            String pid = children.getParentId();
            if ("000000".equals(pid)) {
                res.add(children);
                continue;
            }

            for (SysMenuDto parent : nodes) {
                String id = parent.getId();
                if (id != null && id.equals(pid)) {

                    parent.getChildren().add(children);

                    continue;
                }
            }
        }

        for (SysMenuDto children : res) {

            children.setKey(children.getId());
            String pid = children.getParentId();
            if (HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(pid) || HeaderInfoDto.AUTH_AGENT_SYSTEM.equals(pid)
                    || HeaderInfoDto.AUTH_MERCHANT_SYSTEM.equals(pid)) {
                res.add(children);
                continue;
            }

            for (SysMenuDto parent : res) {
                String id = parent.getId();
                if (id != null && id.equals(pid)) {

                    parent.getChildren().add(children);

                    continue;
                }
            }
        }

        return res;
    }

    /**
     * 返回特定格式的组织机构
     * @param nodes
     * @return
     */
    public static List<Map<String, Object>> buildOfficeMap(List<SysOfficeDto> nodes) {

        if(nodes == null){
            return null;
        }
        List<Map<String, Object>> topNodes = new ArrayList<>();

        Map<String, Object> node = new HashMap<>();
        node.put("value", "");
        node.put("label", "请选择");

        topNodes.add(node);

        for (SysOfficeDto parent : nodes) {

            String pid = parent.getParentId();
            if ("0".equals(pid)) {
                node = new HashMap<>();
                node.put("value", parent.getId());
                node.put("label", parent.getName());

                List<Map<String, Object>> childrens = new ArrayList<>();
                for (SysOfficeDto children : nodes) {
                    String id = children.getParentId();
                    if (id != null && id.equals(parent.getId())) {
                        Map<String, Object> childs = new HashMap<>();
                        childs.put("value", children.getId());
                        childs.put("label", children.getName());

                        childrens.add(childs);
                    }
                }
                node.put("children", childrens);
                topNodes.add(node);
            }else{
                continue;
            }
        }

        return topNodes;
    }

}
