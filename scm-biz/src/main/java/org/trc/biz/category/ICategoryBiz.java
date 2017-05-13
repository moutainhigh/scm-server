package org.trc.biz.category;

import org.trc.domain.category.Category;
import org.trc.form.category.TreeNode;

import java.util.List;

/**
 * Created by hzszy on 2017/5/5.
 */
public interface ICategoryBiz {

    /**
     * 修改
     *
     * @param category
     * @param id
     * @return
     * @throws Exception
     */
    void updateCategory(Category category) throws Exception;

    /**
     * 保存
     */
    void saveClassify(Category category) throws Exception;

    /**
     * 获取分类树节点
     *
     * @param categoryForm
     * @return
     * @throws Exception
     */
    List<TreeNode> getTreeNode() throws Exception;

    /**
     * 查询分类编码是否存在
     *
     * @param categoryCategory
     * @return
     * @throws Exception
     */
    Category findCategoryByCategoryCode(String categoryCategory) throws Exception;

}
