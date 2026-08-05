<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=true; section>
    <#if section = "header">
        Sign In to APU-ASC
    <#elseif section = "form">
        <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
            <div class="form-group">
                <label for="username" class="form-label">${msg("usernameOrEmail")}</label>
                <div class="input-wrapper">
                    <div class="input-icon-left">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                    </div>
                    <input tabindex="1" id="username" class="form-input" name="username" value="${(login.username!'')}" type="text" autofocus autocomplete="username" placeholder="Username or Email" required />
                </div>
            </div>

            <div class="form-group">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.375rem;">
                    <label for="password" class="form-label" style="margin-bottom: 0;">${msg("password")}</label>
                    <#if realm.resetPasswordAllowed>
                        <a tabindex="5" href="${client.baseUrl!'/'}${(client.baseUrl??)?then('/forgot-password', '')}" style="font-size: 0.75rem; color: var(--primary); text-decoration: none; font-weight: 500;">${msg("doForgotPassword")}</a>
                    </#if>
                </div>
                <div class="input-wrapper">
                    <div class="input-icon-left">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="11" x="3" y="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                    </div>
                    <input tabindex="2" id="password" class="form-input" name="password" type="password" autocomplete="current-password" placeholder="Password" required />
                    <button class="password-toggle-btn" type="button" aria-label="${msg('showPassword')}" aria-controls="password" data-password-toggle="true">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>
                    </button>
                </div>
            </div>

            <div style="margin-top: 1.25rem;">
                <input tabindex="4" class="btn-primary" name="login" id="kc-login" type="submit" value="${msg("doLogIn")}"/>
            </div>

            <#if realm.password && realm.registrationAllowed??>
                <div class="form-footer">
                    <span>${msg("noAccount")} <a tabindex="6" href="${url.registrationUrl}">${msg("doRegister")}</a></span>
                </div>
            </#if>
        </form>
    </#if>
</@layout.registrationLayout>
