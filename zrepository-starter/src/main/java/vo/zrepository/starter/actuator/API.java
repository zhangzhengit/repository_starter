package vo.zrepository.starter.actuator;

import java.util.List;

import vo.vortex.anno.ZAutowired;
import vo.vortex.anno.ZController;
import vo.vortex.anno.ZRequestMapping;
import vo.vortex.http.request.ZRequestParam;
import vo.vortex.http.response.ZResponse;
import vo.vortex.template.ZModel;
import vo.zrepository.actuator.SqlInvocationLogsEntity;
import vo.zrepository.actuator.SqlInvocationLogsRepository;
import vo.zrepository.core.Page;
import vo.zrepository.core.ZRWrapper;

/**
 *	监控器相关接口，用于查看SQL执行相关信息
 *
 * @author zhangzhen
 * @date 2024年6月1日 下午7:48:09
 *
 */
@ZController
// FIXME 2025年8月25日 下午5:39:04 zhangzhen: 这个里面所有接口都加入登录校验
public class API {


	@ZAutowired
	SqlInvocationLogsRepository sqlInvocationLogsRepository;

	@ZRequestMapping(mapping = { "/repositorylogin" })
	public void login(final ZResponse response) {
		// FIXME 2025年8月25日 下午6:02:04 zhangzhen: 根据AdminConfigurationProperties 配置项来登录和过期token
		response.cookie("token", "OK-登录了-这是随手写的测试信息");
	}

	/**	首页
	 * @param model
	 * @param pn
	 * @param ps
	 * @return
	 */
	@ZRequestMapping(mapping = { "/repository/admin" })
	public String index(final ZModel model, @ZRequestParam(defaultValue = "1") final Integer pn,
			@ZRequestParam(defaultValue = "10") final Integer ps) {
		// FIXME 2024年6月1日 下午7:49:47 zhangzhen : 写这里，展示慢SQL等，先写具体功能点

		final ZRWrapper<SqlInvocationLogsEntity> wrapper = ZRWrapper.wrap(SqlInvocationLogsEntity.class)
				.orderByDesc(SqlInvocationLogsEntity::getId);

		final Page<SqlInvocationLogsEntity> page = this.sqlInvocationLogsRepository.page(wrapper, pn, ps);

		final List<SqlInvocationLogsEntity> list = page.getList();

		model.set("list", list);

		model.set("name", "zhagnsan");
		model.set("content", list);
		model.set("page", page);
		model.set("totalPages", page.getTotalPage());

		model.set("number", page.getPage());
		model.set("size", page.getSize());
		model.set("totalElements", page.getTotalCount());
		model.set("numberOfElements", page.getList().size());

		return "html/actuator_index.html";
	}
}
