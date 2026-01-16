import editForm from "../form.vue";
import { handleTree } from "@/utils/tree";
import { message } from "@/utils/message";
import { getMenuTree, createMenu, updateMenu, deleteMenu, type CreateMenuRequest, type UpdateMenuRequest } from "@/api/menu";
import { transformI18n } from "@/plugins/i18n";
import { addDialog } from "@/components/ReDialog";
import { reactive, ref, onMounted, h } from "vue";
import type { FormItemProps } from "../utils/types";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { cloneDeep, isAllEmpty, deviceDetection } from "@pureadmin/utils";

export function useMenu() {
  const form = reactive({
    title: ""
  });

  const formRef = ref();
  const dataList = ref([]);
  const loading = ref(true);

  // 根据是否有子菜单判断菜单类型
  const getMenuType = (row, text = false) => {
    const hasChildren = row.children && row.children.length > 0;
    if (hasChildren) {
      return text ? "目录" : "primary";
    } else if (row.component && row.component !== "Layout") {
      return text ? "菜单" : "success";
    } else {
      return text ? "外链" : "warning";
    }
  };

  const columns: TableColumnList = [
    {
      label: "菜单名称",
      prop: "title",
      align: "left",
      cellRenderer: ({ row }) => (
        <>
          <span class="inline-block mr-1">
            {h(useRenderIcon(row.icon), {
              style: { paddingTop: "1px" }
            })}
          </span>
          <span>{transformI18n(row.title)}</span>
        </>
      )
    },
    {
      label: "菜单类型",
      prop: "menuType",
      width: 100,
      cellRenderer: ({ row, props }) => (
        <el-tag
          size={props.size}
          type={getMenuType(row)}
          effect="plain"
        >
          {getMenuType(row, true)}
        </el-tag>
      )
    },
    {
      label: "路由路径",
      prop: "path"
    },
    {
      label: "组件路径",
      prop: "component",
      formatter: ({ component }) => component || "-"
    },
    {
      label: "排序",
      prop: "sortOrder",
      width: 100,
      sortable: true
    },
    {
      label: "显示",
      prop: "showLink",
      formatter: ({ showLink }) => (showLink === 1 ? "是" : "否"),
      width: 100
    },
    {
      label: "状态",
      prop: "status",
      width: 100,
      cellRenderer: ({ row, props }) => (
        <el-tag
          size={props.size}
          type={row.status === 1 ? "success" : "danger"}
          effect="plain"
        >
          {row.status === 1 ? "启用" : "禁用"}
        </el-tag>
      )
    },
    {
      label: "操作",
      fixed: "right",
      width: 210,
      slot: "operation"
    }
  ];

  function handleSelectionChange(val) {
    // 调试用，已注释
    // console.log("handleSelectionChange", val);
  }

  function resetForm(formEl) {
    if (!formEl) return;
    formEl.resetFields();
    onSearch();
  }

  async function onSearch() {
    loading.value = true;
    try {
      const response = await getMenuTree();
      // 后端返回 code: 200 表示成功
      if (response.code === 200 && response.data) {
        let newData = response.data;
        if (!isAllEmpty(form.title)) {
          // 前端搜索菜单名称（递归搜索）
          const filterTree = (tree: any[]): any[] => {
            return tree
              .map(item => {
                const match = transformI18n(item.title).includes(form.title);
                const children = item.children ? filterTree(item.children) : [];
                if (match || children.length > 0) {
                  return { ...item, children: children.length > 0 ? children : item.children };
                }
                return null;
              })
              .filter(Boolean);
          };
          newData = filterTree(newData);
        }
        dataList.value = newData; // 后端已返回树结构
      } else {
        message(response.message || "获取菜单列表失败", { type: "error" });
        dataList.value = [];
      }
    } catch (error: any) {
      message(error.message || "获取菜单列表失败", { type: "error" });
      dataList.value = [];
    } finally {
      loading.value = false;
    }
  }

  function formatHigherMenuOptions(treeList) {
    if (!treeList || !treeList.length) return;
    const newTreeList = [];
    for (let i = 0; i < treeList.length; i++) {
      treeList[i].title = transformI18n(treeList[i].title);
      formatHigherMenuOptions(treeList[i].children);
      newTreeList.push(treeList[i]);
    }
    return newTreeList;
  }

  function openDialog(title = "新增", row?: FormItemProps) {
    addDialog({
      title: `${title}菜单`,
      props: {
        formInline: {
          id: row?.id,
          menuType: row?.menuType ?? 0,
          higherMenuOptions: formatHigherMenuOptions(cloneDeep(dataList.value)),
          parentId: row?.parentId ?? 0,
          title: row?.title ?? "",
          name: row?.name ?? "",
          path: row?.path ?? "",
          component: row?.component ?? "",
          sortOrder: row?.sortOrder ?? 0,
          redirect: row?.redirect ?? "",
          icon: row?.icon ?? "",
          extraIcon: row?.extraIcon ?? "",
          enterTransition: row?.enterTransition ?? "",
          leaveTransition: row?.leaveTransition ?? "",
          activePath: row?.activePath ?? "",
          auths: row?.auths ?? "",
          frameSrc: row?.frameSrc ?? "",
          frameLoading: row?.frameLoading ?? true,
          keepAlive: row?.keepAlive ?? false,
          hiddenTag: row?.hiddenTag ?? false,
          fixedTag: row?.fixedTag ?? false,
          showLink: row?.showLink ?? true,
          showParent: row?.showParent ?? false,
          status: row?.status ?? 1
        }
      },
      width: "45%",
      draggable: true,
      fullscreen: deviceDetection(),
      fullscreenIcon: true,
      closeOnClickModal: false,
      contentRenderer: () => h(editForm, { ref: formRef, formInline: null }),
      beforeSure: async (done, { options }) => {
        const FormRef = formRef.value.getRef();
        const curData = options.props.formInline as FormItemProps;
        FormRef.validate(async (valid: boolean) => {
          if (valid) {
            try {
              if (title === "新增") {
                const request: CreateMenuRequest = {
                  parentId: curData.parentId || 0,
                  name: curData.name,
                  path: curData.path,
                  component: curData.component || undefined,
                  redirect: curData.redirect || undefined,
                  icon: curData.icon || undefined,
                  title: curData.title,
                  sortOrder: curData.sortOrder || 0,
                  showLink: curData.showLink ? 1 : 0,
                  keepAlive: curData.keepAlive ? 1 : 0,
                  status: 1
                };
                const response = await createMenu(request);
                if (response.code === 200) {
                  message(`您${title}了菜单名称为${transformI18n(curData.title)}的这条数据`, {
                    type: "success"
                  });
                  done();
                  onSearch();
                } else {
                  message(response.message || "创建菜单失败", { type: "error" });
                }
              } else {
                const request: UpdateMenuRequest = {
                  parentId: curData.parentId !== undefined ? curData.parentId : undefined,
                  name: curData.name || undefined,
                  path: curData.path || undefined,
                  component: curData.component || undefined,
                  redirect: curData.redirect || undefined,
                  icon: curData.icon || undefined,
                  title: curData.title || undefined,
                  sortOrder: curData.sortOrder !== undefined ? curData.sortOrder : undefined,
                  showLink: curData.showLink !== undefined ? (curData.showLink ? 1 : 0) : undefined,
                  keepAlive: curData.keepAlive !== undefined ? (curData.keepAlive ? 1 : 0) : undefined,
                  status: curData.status !== undefined ? curData.status : undefined
                };
                const response = await updateMenu(curData.id, request);
                if (response.code === 200) {
                  message(`您${title}了菜单名称为${transformI18n(curData.title)}的这条数据`, {
                    type: "success"
                  });
                  done();
                  onSearch();
                } else {
                  message(response.message || "更新菜单失败", { type: "error" });
                }
              }
            } catch (error: any) {
              message(error.message || `${title}菜单失败`, { type: "error" });
            }
          }
        });
      }
    });
  }

  async function handleDelete(row) {
    try {
      const response = await deleteMenu(row.id);
      if (response.code === 200) {
        message(`您删除了菜单名称为${transformI18n(row.title)}的这条数据`, {
          type: "success"
        });
        onSearch();
      } else {
        message(response.message || "删除菜单失败", { type: "error" });
      }
    } catch (error: any) {
      message(error.message || "删除菜单失败", { type: "error" });
    }
  }

  onMounted(() => {
    onSearch();
  });

  return {
    form,
    loading,
    columns,
    dataList,
    /** 搜索 */
    onSearch,
    /** 重置 */
    resetForm,
    /** 新增、修改菜单 */
    openDialog,
    /** 删除菜单 */
    handleDelete,
    handleSelectionChange
  };
}
