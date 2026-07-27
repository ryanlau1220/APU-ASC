<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        <#if messageHeader??>
            ${messageHeader}
        <#else>
            ${message.summary}
        </#if>
    <#elseif section = "form">
        <div style="text-align: center; margin-bottom: 1.5rem;">
            <p style="color: #cbd5e1; font-size: 0.875rem; margin-bottom: 1.5rem; line-height: 1.5;">${message.summary}</p>
            <#if actionUri??>
                <a href="${actionUri}" class="btn-primary">${msg("proceedWithAction")}</a>
            <#elseif client?? && client.baseUrl??>
                <a href="http://localhost:3000/oauth2/authorization/keycloak" class="btn-primary">${msg("backToApplication")}</a>
            <#else>
                <a href="http://localhost:3000/oauth2/authorization/keycloak" class="btn-primary">Proceed to APU-ASC Sign In</a>
            </#if>
        </div>
    </#if>
</@layout.registrationLayout>
