<!DOCTYPE html>

<#include init />

<html class="${root_css_class}" dir="<@liferay.language key="lang.dir" />" lang="${w3c_language_id}">

<head>
	<title>${the_title} - ${company_name}</title>

	<meta content="initial-scale=1.0, width=device-width" name="viewport" />

	<@liferay_util["include"] page=top_head_include />
</head>

<body class="${css_class}">

<@liferay_ui["quick-access"] contentId="#main-content" />

<@liferay_util["include"] page=body_top_include />

<@liferay.control_menu />

<div class="container-fluid" id="wrapper">
	<header id="banner" role="banner">
		<div id="heading">
			<h1 class="site-title">
				<a class="${logo_css_class}" href="${site_default_url}" title="<@liferay.language_format arguments="${site_name}" key="go-to-x" />">
					<img alt="${logo_description}" height="${site_logo_height}" src="${site_logo}" width="${site_logo_width}" />
				</a>

				<#if show_site_name>
					<span class="site-name" title="<@liferay.language_format arguments="${site_name}" key="go-to-x" />">
						${site_name}
					</span>
				</#if>
			</h1>
		</div>

		<#if !is_signed_in>
			<a data-redirect="${is_login_redirect_required?string}" href="${sign_in_url}" id="sign-in" rel="nofollow">${sign_in_text}</a>
		</#if>

		<#if has_navigation && is_setup_complete && site_navigation_menus??>
			<#assign header = "Header" />

			<h2>${site_navigation_menus[header]}</h2>
			<#assign primaryNavigationPreferencesMap = {"displayStyle": "ddmTemplate_CUSTOM-NAVIGATION", "displayStyleGroupId": "${group_id}", "siteNavigationMenuId": "${site_navigation_menus[header]}", "portletSetupPortletDecoratorId": "barebone"} />

			<@liferay.navigation_menu
				default_preferences=freeMarkerPortletPreferences.getPreferences(primaryNavigationPreferencesMap)
				instance_id="main_navigation_menu"
			/>
		</#if>
	</header>

	<section id="content">
		<h1 class="hide-accessible">${the_title}</h1>

		<#if selectable>
			<@liferay_util["include"] page=content_include />
		<#else>
			${portletDisplay.recycle()}

			${portletDisplay.setTitle(the_title)}

			<@liferay_theme["wrap-portlet"] page="portlet.ftl">
				<@liferay_util["include"] page=content_include />
			</@>
		</#if>
	</section>

	<footer id="footer" role="contentinfo">
		<#if site_navigation_menus??>
			<#assign footer = "Footer" />

			<h2>${site_navigation_menus[footer]}</h2>

			<#assign secondaryNavigationPreferencesMap = {"displayStyle": "ddmTemplate_CUSTOM-NAVIGATION", "displayStyleGroupId": "${group_id}", "siteNavigationMenuId": "${site_navigation_menus[footer]}", "portletSetupPortletDecoratorId": "barebone"} />

			<@liferay.navigation_menu
				default_preferences=freeMarkerPortletPreferences.getPreferences(secondaryNavigationPreferencesMap)
				instance_id="secondary_navigation_menu"
			/>
		</#if>

		<p class="powered-by">
			<@liferay.language key="powered-by" /> <a href="http://www.liferay.com" rel="external">Liferay</a>
		</p>
	</footer>
</div>

<@liferay_util["include"] page=body_bottom_include />

<@liferay_util["include"] page=bottom_include />

<!-- inject:js -->
<!-- endinject -->

</body>

</html>