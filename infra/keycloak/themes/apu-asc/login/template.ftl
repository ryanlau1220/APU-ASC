<#macro registrationLayout bodyClass="" displayHeader=true displayMessage=true displayRequiredFields=false displayInfo=false displayWide=false>
<!DOCTYPE html>
<html class="${properties.kcHtmlClass!}" lang="${(locale.currentLanguageTag)!'en'}">

<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="robots" content="noindex, nofollow">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>${msg("loginTitle",(realm.displayName!'APU Automotive Service Centre'))}</title>
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico" />
    <#if properties.stylesCommon??>
        <#list properties.stylesCommon?split(' ') as style>
            <link href="${url.resourcesCommonPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.styles??>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.scripts??>
        <#list properties.scripts?split(' ') as script>
            <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <#if scripts??>
        <#list scripts as script>
            <script src="${script}" type="text/javascript"></script>
        </#list>
    </#if>
</head>

<body class="${properties.kcBodyClass!}">

  <!-- APU-ASC Top Navigation Header -->
  <header class="apu-top-header">
    <div class="apu-header-content">
      <a href="http://localhost:3000/" class="apu-brand-logo">
        <div class="apu-logo-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg>
        </div>
        <span class="apu-brand-name">APU-ASC</span>
      </a>
    </div>
  </header>

  <main class="apu-main-wrapper">
    <div class="card-pf">
      <header class="card-pf-header">
        <div class="apu-card-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg>
        </div>
        <h1 id="kc-page-title">
          <#nested "header">
        </h1>
      </header>

      <div id="kc-content">
        <div id="kc-content-wrapper">
          <#-- App-initiated actions should not be dismissed -->
          <#if displayMessage && message?? && (message.type != 'warning' || !isAppInitiatedAction??)>
              <div class="alert alert-${message.type} ${properties.kcFeedbackAreaClass!}">
                  <span class="${properties.kcFeedbackIcon!}"></span>
                  <span class="kc-feedback-text">${kcSanitize(message.summary)?no_esc}</span>
              </div>
          </#if>

          <#nested "form">

          <#if displayInfo?? && displayInfo>
              <div id="kc-info" class="${properties.kcSignUpClass!}">
                  <div id="kc-info-wrapper" class="${properties.kcSignUpClassWrapper!}">
                      <#nested "info">
                  </div>
              </div>
          </#if>
        </div>
      </div>

    </div>
  </main>

  <script>
    document.addEventListener('DOMContentLoaded', function() {
      document.body.addEventListener('click', function(e) {
        var btn = e.target.closest('button[data-password-toggle], .kc-password-toggle, button.pf-c-button');
        if (btn) {
          e.preventDefault();
          var targetId = btn.getAttribute('aria-controls') || 'password';
          var input = document.getElementById(targetId) || btn.parentElement.querySelector('input');
          if (input) {
            if (input.type === 'password') {
              input.type = 'text';
              btn.setAttribute('aria-label', 'Hide password');
            } else {
              input.type = 'password';
              btn.setAttribute('aria-label', 'Show password');
            }
          }
        }
      });
    });
  </script>
</body>
</html>
</#macro>
