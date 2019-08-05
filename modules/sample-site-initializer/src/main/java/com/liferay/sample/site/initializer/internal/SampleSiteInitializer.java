/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.sample.site.initializer.internal;

import com.liferay.site.navigation.model.SiteNavigationMenu;
import com.liferay.site.navigation.service.SiteNavigationMenuService;
import com.liferay.site.navigation.service.SiteNavigationMenuItemService;
import com.liferay.site.navigation.constants.SiteNavigationConstants;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.model.DDMTemplateConstants;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.Theme;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.LayoutService;
import com.liferay.portal.kernel.service.LayoutSetLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ThemeLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.template.TemplateConstants;
import com.liferay.portal.kernel.theme.NavItem;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.site.exception.InitializationException;
import com.liferay.site.initializer.SiteInitializer;

import java.net.URL;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Travis Cory
 */
@Component(
	immediate = true,
	property = "site.initializer.key=" + SampleSiteInitializer.KEY,
	service = SiteInitializer.class
)
public class SampleSiteInitializer implements SiteInitializer {

	public static final String KEY = "sample-site-initializer";

	@Override
	public String getDescription(Locale locale) {
		return StringPool.BLANK;
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName(Locale locale) {
		return _THEME_NAME;
	}

	@Override
	public String getThumbnailSrc() {
		return _servletContext.getContextPath() + "/images/thumbnail.png";
	}

	@Override
	public void initialize(long groupId) throws InitializationException {
		try {
			ServiceContext serviceContext = _createServiceContext(groupId);

			_updateLookAndFeel(serviceContext);
			_addApplicationDisplayTemplates(serviceContext);

			SiteNavigationMenu headerMenu = _addSiteNavigationMenu("Header", SiteNavigationConstants.TYPE_DEFAULT, serviceContext);
			SiteNavigationMenu footerMenu = _addSiteNavigationMenu("Footer", SiteNavigationConstants.TYPE_DEFAULT, serviceContext);

			_addLayoutWithNameAndAddToMenu("Home", headerMenu.getSiteNavigationMenuId(), serviceContext);
			_addLayoutWithNameAndAddToMenu("About Us", headerMenu.getSiteNavigationMenuId(), serviceContext);
			_addLayoutWithNameAndAddToMenu("Learn More", headerMenu.getSiteNavigationMenuId(), serviceContext);

			_addLayoutWithNameAndAddToMenu("Contact Us", footerMenu.getSiteNavigationMenuId(), serviceContext);
			_addLayoutWithNameAndAddToMenu("Footer Layout", footerMenu.getSiteNavigationMenuId(), serviceContext);

		}
		catch (Exception e) {
			_log.error(e, e);

			throw new InitializationException(e);
		}
	}

	private SiteNavigationMenu _addSiteNavigationMenu(String name, int type, ServiceContext serviceContext) throws Exception {
		return _siteNavigationMenuService.addSiteNavigationMenu(
			serviceContext.getScopeGroupId(), name, type, false,
			serviceContext);
	}

	@Override
	public boolean isActive(long companyId) {
		return true;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundle = bundleContext.getBundle();
	}

	private void _addApplicationDisplayTemplates(
			ServiceContext serviceContext)
		throws Exception {

		Enumeration<URL> enumeration = _bundle.findEntries(
			_PATH + "/adt", "*.ftl", false);

		while (enumeration.hasMoreElements()) {
			URL url = enumeration.nextElement();

			String script = StringUtil.read(url.openStream());

			String fileName = FileUtil.stripExtension(
				FileUtil.getShortFileName(url.getPath()));

			Map<Locale, String> nameMap = new HashMap<>();

			nameMap.put(LocaleUtil.getSiteDefault(), fileName);

			DDMTemplate ddmTemplate = _ddmTemplateLocalService.addTemplate(
				serviceContext.getUserId(), serviceContext.getScopeGroupId(),
				_portal.getClassNameId(NavItem.class.getName()), 0,
				_portal.getClassNameId(_PORTLET_DISPLAY_TEMPLATE_CLASS_NAME),
				nameMap, new HashMap<>(),
				DDMTemplateConstants.TEMPLATE_TYPE_DISPLAY,
				DDMTemplateConstants.TEMPLATE_MODE_EDIT,
				TemplateConstants.LANG_TYPE_FTL, script, serviceContext);

			ddmTemplate.setTemplateKey(fileName.toUpperCase().replaceAll(" ", "-"));

			_ddmTemplateLocalService.updateDDMTemplate(ddmTemplate);
		}
	}

	private Layout _addLayout(String name, ServiceContext serviceContext)
		throws Exception {

		Map<Locale, String> nameMap = new HashMap<>();

		nameMap.put(LocaleUtil.getSiteDefault(), name);

		UnicodeProperties typeSettingsProperties = new UnicodeProperties();

		typeSettingsProperties.put("layout-template-id", "1_column");

		typeSettingsProperties.put("groupId", String.valueOf(serviceContext.getScopeGroupId()));

		return _layoutLocalService.addLayout(
			serviceContext.getUserId(), serviceContext.getScopeGroupId(), false,
			LayoutConstants.DEFAULT_PARENT_LAYOUT_ID, nameMap, new HashMap<>(),
			new HashMap<>(), new HashMap<>(), new HashMap<>(), "portlet",
			typeSettingsProperties.toString(), false, new HashMap<>(),
			serviceContext);
	}

	private void _addLayoutWithNameAndAddToMenu(String name, long menuId, ServiceContext serviceContext) throws Exception {
		Layout layout = _addLayout(name, serviceContext);

		long parentId = 0;

		UnicodeProperties typeSettingsProperties = new UnicodeProperties();

		typeSettingsProperties.put("groupId", String.valueOf(serviceContext.getScopeGroupId()));

		typeSettingsProperties.put("layoutUuid", layout.getUuid());
		typeSettingsProperties.put("privateLayout", String.valueOf(layout.isPrivateLayout()));
		typeSettingsProperties.put("title", layout.getNameCurrentValue());

		_siteNavigationMenuItemService.addSiteNavigationMenuItem(
			serviceContext.getScopeGroupId(), menuId, parentId,
			"layout", typeSettingsProperties.toString(), serviceContext);
	}

	private ServiceContext _createServiceContext(long groupId)
		throws PortalException {

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);

		Group group = _groupLocalService.getGroup(groupId);

		serviceContext.setCompanyId(group.getCompanyId());

		User user = _userLocalService.getUser(PrincipalThreadLocal.getUserId());

		Locale locale = LocaleUtil.getSiteDefault();

		serviceContext.setLanguageId(LanguageUtil.getLanguageId(locale));

		serviceContext.setScopeGroupId(groupId);
		serviceContext.setTimeZone(user.getTimeZone());
		serviceContext.setUserId(user.getUserId());

		return serviceContext;
	}
	private void _updateLookAndFeel(ServiceContext serviceContext)
		throws PortalException {

		Theme theme = _themeLocalService.fetchTheme(
			serviceContext.getCompanyId(), _THEME_ID);

		if (theme == null) {
			if (_log.isInfoEnabled()) {
				_log.info("No theme found for " + _THEME_ID);
			}

			return;
		}

		_layoutSetLocalService.updateLookAndFeel(
			serviceContext.getScopeGroupId(), _THEME_ID, StringPool.BLANK,
			StringPool.BLANK);
	}

	private static final String _PATH =
		"com/liferay/sample/site/initializer/internal/dependencies";


	private static final String _PORTLET_DISPLAY_TEMPLATE_CLASS_NAME =
		"com.liferay.portlet.display.template.PortletDisplayTemplate";

	private static final String _THEME_ID = "sample_WAR_sampletheme";

	private static final String _THEME_NAME = "Sample";

	private static final Log _log = LogFactoryUtil.getLog(
		SampleSiteInitializer.class);

	private Bundle _bundle;

	@Reference
	private DDMTemplateLocalService _ddmTemplateLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private SiteNavigationMenuService _siteNavigationMenuService;

	@Reference
	private SiteNavigationMenuItemService _siteNavigationMenuItemService;

	@Reference
	private LayoutLocalService _layoutLocalService;


	@Reference
	private LayoutService _layoutService;

	@Reference
	private LayoutSetLocalService _layoutSetLocalService;

	@Reference
	private Portal _portal;

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.sample.site.initializer)"
	)
	private ServletContext _servletContext;

	@Reference
	private ThemeLocalService _themeLocalService;

	@Reference
	private UserLocalService _userLocalService;

}