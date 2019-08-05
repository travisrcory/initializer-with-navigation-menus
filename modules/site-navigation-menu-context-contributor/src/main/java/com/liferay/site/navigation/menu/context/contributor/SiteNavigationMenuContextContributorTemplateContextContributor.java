package com.liferay.site.navigation.menu.context.contributor;

import com.liferay.portal.kernel.template.TemplateContextContributor;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.navigation.model.SiteNavigationMenu;
import com.liferay.site.navigation.service.SiteNavigationMenuService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Travis Cory
 */
@Component(
	immediate = true,
	property = {"type=" + TemplateContextContributor.TYPE_THEME},
	service = TemplateContextContributor.class
)
public class SiteNavigationMenuContextContributorTemplateContextContributor
	implements TemplateContextContributor {

	@Override
	public void prepare(
		Map<String, Object> contextObjects, HttpServletRequest request) {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)request.getAttribute(
				WebKeys.THEME_DISPLAY);

		Map<String, Object> siteNavigationMenuMap = new HashMap<String, Object>();

		List<SiteNavigationMenu> siteNavigationMenus = _siteNavigationMenuService.getSiteNavigationMenus(themeDisplay.getScopeGroupId());

		for (SiteNavigationMenu siteNavigationMenu : siteNavigationMenus) {
			siteNavigationMenuMap.put(siteNavigationMenu.getName(), siteNavigationMenu.getSiteNavigationMenuId());
		}

		contextObjects.put("site_navigation_menus", siteNavigationMenuMap);
	}


	@Reference
	private SiteNavigationMenuService _siteNavigationMenuService;

}