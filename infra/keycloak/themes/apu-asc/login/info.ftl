<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        <#if messageHeader??>
            ${messageHeader}
        <#else>
            ${message.summary}
        </#if>
    <#elseif section = "form">
        <div id="kc-info-message" class="text-center space-y-4">
            <p class="instruction" style="color: #cbd5e1; font-size: 0.875rem; margin-bottom: 20px;">
                <#if message??>
                    ${kcSanitize(message.summary)?no_esc}
                </#if>
            </p>

            <#if actionUri??>
                <p><a href="${actionUri}" class="btn-primary" style="display: inline-block; text-decoration: none; text-align: center;">${msg("proceedWithAction")}</a></p>
            <#elseif (client.baseUrl)??>
                <p><a href="${client.baseUrl}" class="btn-primary" style="display: inline-block; text-decoration: none; text-align: center;">${msg("backToApplication")}</a></p>
            <#else>
                <p><a href="http://localhost:3000/login" class="btn-primary" style="display: inline-block; text-decoration: none; text-align: center;">Proceed to APU-ASC Login</a></p>
            </#if>

            <script>
                // Automatically redirect to the APU-ASC login screen after 3 seconds
                setTimeout(function() {
                    window.location.href = "${(client.baseUrl)!'http://localhost:3000/login'}";
                }, 3000);
            </script>
        </div>
    </#if>
</@layout.registrationLayout>
