<#macro registrationLayout displayMessage=true displayRequiredFields=false showAnotherWay=false displayInfo=false>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="robots" content="noindex, nofollow">
    <title><#nested "header"></title>
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico" />
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=Outfit:wght@500;600;700;800&display=swap" rel="stylesheet">
    <style>
        *, ::before, ::after { box-sizing: border-box; margin: 0; padding: 0; }
        
        :root {
            --bg: #090d16;
            --card: #0f172a;
            --border: #1e293b;
            --input-bg: #0b0f19;
            --input-border: #334155;
            --text-main: #f8fafc;
            --text-muted: #94a3b8;
            --primary: #0284c7;
            --primary-hover: #0369a1;
            --icon-color: #64748b;
        }

        @media (prefers-color-scheme: light) {
            :root {
                --bg: #f8fafc;
                --card: #ffffff;
                --border: #e2e8f0;
                --input-bg: #ffffff;
                --input-border: #cbd5e1;
                --text-main: #0f172a;
                --text-muted: #64748b;
                --primary: #0284c7;
                --primary-hover: #0369a1;
                --icon-color: #94a3b8;
            }
        }

        body {
            font-family: 'Inter', system-ui, -apple-system, sans-serif;
            background-color: var(--bg);
            color: var(--text-main);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 1.5rem 1rem;
            -webkit-font-smoothing: antialiased;
            transition: background-color 0.2s, color 0.2s;
        }
        .auth-container {
            width: 100%;
            max-width: 420px;
            margin: 0 auto;
        }
        .auth-card {
            background-color: var(--card);
            border: 1px solid var(--border);
            border-radius: 0.75rem;
            padding: 2rem;
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -4px rgba(0, 0, 0, 0.1);
        }
        .brand-header {
            text-align: center;
            margin-bottom: 1.5rem;
        }
        .brand-logo {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 3rem;
            height: 3rem;
            background-color: rgba(2, 132, 199, 0.12);
            color: var(--primary);
            border-radius: 0.625rem;
            margin-bottom: 0.875rem;
            text-decoration: none;
        }
        .brand-title {
            font-family: 'Outfit', sans-serif;
            font-size: 1.375rem;
            font-weight: 700;
            color: var(--text-main);
            letter-spacing: -0.025em;
        }
        .brand-subtitle {
            font-size: 0.75rem;
            color: var(--text-muted);
            margin-top: 0.25rem;
        }
        .form-group {
            margin-bottom: 1.125rem;
        }
        .form-label {
            display: block;
            font-size: 0.75rem;
            font-weight: 600;
            color: var(--text-main);
            margin-bottom: 0.375rem;
        }
        .input-wrapper {
            position: relative;
            display: flex;
            align-items: center;
        }
        .input-icon-left {
            position: absolute;
            left: 0.75rem;
            color: var(--icon-color);
            pointer-events: none;
            display: flex;
            align-items: center;
        }
        .form-input {
            width: 100%;
            padding: 0.625rem 0.75rem 0.625rem 2.375rem;
            background-color: var(--input-bg);
            border: 1px solid var(--input-border);
            border-radius: 0.5rem;
            color: var(--text-main);
            font-size: 0.8125rem;
            outline: none;
            transition: border-color 0.15s, box-shadow 0.15s;
        }
        .form-input.no-icon {
            padding-left: 0.75rem;
        }
        .form-input:focus {
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(2, 132, 199, 0.2);
        }
        .password-toggle-btn {
            position: absolute;
            right: 0.75rem;
            background: none;
            border: none;
            color: var(--icon-color);
            cursor: pointer;
            padding: 0.25rem;
            display: flex;
            align-items: center;
        }
        .password-toggle-btn:hover {
            color: var(--text-main);
        }
        .btn-primary {
            width: 100%;
            padding: 0.6875rem 1rem;
            background-color: var(--primary);
            color: #ffffff;
            font-weight: 600;
            font-size: 0.8125rem;
            border: none;
            border-radius: 0.5rem;
            cursor: pointer;
            transition: background-color 0.15s, opacity 0.15s;
            text-decoration: none;
            display: inline-block;
            text-align: center;
        }
        .btn-primary:hover {
            background-color: var(--primary-hover);
        }
        .alert-error {
            background-color: rgba(239, 68, 68, 0.1);
            border: 1px solid rgba(239, 68, 68, 0.3);
            color: #ef4444;
            padding: 0.625rem 0.75rem;
            border-radius: 0.5rem;
            font-size: 0.75rem;
            font-weight: 500;
            margin-bottom: 1.125rem;
        }
        .alert-success {
            background-color: rgba(34, 197, 94, 0.1);
            border: 1px solid rgba(34, 197, 94, 0.3);
            color: #22c55e;
            padding: 0.625rem 0.75rem;
            border-radius: 0.5rem;
            font-size: 0.75rem;
            font-weight: 500;
            margin-bottom: 1.125rem;
        }
        .form-footer {
            margin-top: 1.25rem;
            text-align: center;
            font-size: 0.75rem;
            color: var(--text-muted);
        }
        .form-footer a {
            color: var(--primary);
            text-decoration: none;
            font-weight: 600;
        }
        .form-footer a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="auth-container">
        <div class="auth-card">
            <div class="brand-header">
                <a href="${client.baseUrl!'/'}" class="brand-logo">
                    <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg>
                </a>
                <h1 class="brand-title"><#nested "header"></h1>
                <p class="brand-subtitle">APU Automotive Service Centre</p>
            </div>

            <#if displayMessage && message?? && (message.type != 'warning' || !isAppInitiatedAction??)>
                <div class="<#if message.type = 'error'>alert-error<#else>alert-success</#if>">
                    ${kcSanitize(message.summary)?no_esc}
                </div>
            </#if>

            <#nested "form">
        </div>
    </div>

    <script>
        document.querySelectorAll('[data-password-toggle]').forEach(function(btn) {
            btn.addEventListener('click', function() {
                var targetId = btn.getAttribute('aria-controls');
                var input = document.getElementById(targetId);
                if (input) {
                    if (input.type === 'password') {
                        input.type = 'text';
                    } else {
                        input.type = 'password';
                    }
                }
            });
        });
    </script>
</body>
</html>
</#macro>
