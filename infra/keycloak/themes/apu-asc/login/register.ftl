<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=true; section>
    <#if section = "header">
        Create APU-ASC Account
    <#elseif section = "form">
        <form id="kc-register-form" action="${url.registrationAction}" method="post">
            <div class="form-group">
                <label for="fullName" class="form-label">${msg("fullName")}</label>
                <div class="input-wrapper">
                    <div class="input-icon-left">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                    </div>
                    <input type="text" id="fullName" class="form-input" value="${(register.formData.firstName!'')}<#if register.formData.lastName?? && register.formData.lastName != '.'> ${register.formData.lastName}</#if>" placeholder="Full Name" required autofocus />
                </div>
                <input type="hidden" id="firstName" name="firstName" value="${(register.formData.firstName!'')}" />
                <input type="hidden" id="lastName" name="lastName" value="${(register.formData.lastName!'.')}" />
            </div>

            <div class="form-group">
                <label for="email" class="form-label">${msg("email")}</label>
                <div class="input-wrapper">
                    <div class="input-icon-left">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="20" height="16" x="2" y="4" rx="2"/><path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/></svg>
                    </div>
                    <input type="email" id="email" class="form-input" name="email" value="${(register.formData.email!'')}" autocomplete="email" placeholder="user@example.com" required />
                </div>
            </div>

            <#if !realm.registrationEmailAsUsername>
                <div class="form-group">
                    <label for="username" class="form-label">${msg("username")}</label>
                    <div class="input-wrapper">
                        <div class="input-icon-left">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                        </div>
                        <input type="text" id="username" class="form-input" name="username" value="${(register.formData.username!'')}" autocomplete="username" placeholder="Username" required />
                    </div>
                </div>
            </#if>

            <#if passwordRequired??>
                <div class="form-group">
                    <label for="password" class="form-label">${msg("password")}</label>
                    <div class="input-wrapper">
                        <div class="input-icon-left">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="11" x="3" y="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                        </div>
                        <input type="password" id="password" class="form-input" name="password" autocomplete="new-password" placeholder="Minimum 8 characters" required minlength="8" />
                        <button class="password-toggle-btn" type="button" aria-label="${msg('showPassword')}" aria-controls="password" data-password-toggle="true">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>
                        </button>
                    </div>
                </div>

                <div class="form-group">
                    <label for="password-confirm" class="form-label">${msg("passwordConfirm")}</label>
                    <div class="input-wrapper">
                        <div class="input-icon-left">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="11" x="3" y="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                        </div>
                        <input type="password" id="password-confirm" class="form-input" name="password-confirm" placeholder="Re-enter password" required minlength="8" />
                        <button class="password-toggle-btn" type="button" aria-label="${msg('showPassword')}" aria-controls="password-confirm" data-password-toggle="true">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>
                        </button>
                    </div>
                </div>
            </#if>

            <div style="margin-top: 1.25rem;">
                <input class="btn-primary" type="submit" value="${msg("doRegister")}"/>
            </div>

            <div class="form-footer">
                <span>Already have an account? <a href="${url.loginUrl}">${msg("backToLogin")}</a></span>
            </div>
        </form>

        <script>
            document.getElementById('kc-register-form').addEventListener('submit', function() {
                var nameVal = document.getElementById('fullName').value.trim();
                if (nameVal) {
                    var parts = nameVal.split(/\s+/);
                    var first = parts[0];
                    var last = parts.slice(1).join(' ') || '.';
                    document.getElementById('firstName').value = first;
                    document.getElementById('lastName').value = last;
                } else {
                    document.getElementById('firstName').value = 'User';
                    document.getElementById('lastName').value = '.';
                }
            });
        </script>
    </#if>
</@layout.registrationLayout>
