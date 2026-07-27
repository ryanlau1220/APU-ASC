<#macro registrationLayout displayMessage=true displayRequiredFields=false showAnotherWay=false>
<!DOCTYPE html>
<html lang="en" class="dark">
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
        body {
            font-family: 'Inter', system-ui, -apple-system, sans-serif;
            background-color: #0b0f19;
            color: #f8fafc;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 1.5rem 1rem;
            -webkit-font-smoothing: antialiased;
        }
        .auth-container {
            width: 100%;
            max-width: 440px;
            margin: 0 auto;
        }
        .auth-card {
            background-color: #131c31;
            border: 1px solid #1e293b;
            border-radius: 1rem;
            padding: 2rem;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5), 0 8px 10px -6px rgba(0, 0, 0, 0.5);
        }
        .brand-header {
            text-align: center;
            margin-bottom: 1.75rem;
        }
        .brand-logo {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 3.25rem;
            height: 3.25rem;
            background-color: rgba(2, 132, 199, 0.15);
            color: #38bdf8;
            border-radius: 0.75rem;
            margin-bottom: 1rem;
            text-decoration: none;
        }
        .brand-title {
            font-family: 'Outfit', sans-serif;
            font-size: 1.5rem;
            font-weight: 700;
            color: #f8fafc;
            letter-spacing: -0.025em;
        }
        .brand-subtitle {
            font-size: 0.8125rem;
            color: #94a3b8;
            margin-top: 0.25rem;
        }
        .form-group {
            margin-bottom: 1.25rem;
        }
        .form-label {
            display: block;
            font-size: 0.8125rem;
            font-weight: 600;
            color: #cbd5e1;
            margin-bottom: 0.375rem;
        }
        .form-input {
            width: 100%;
            padding: 0.625rem 0.875rem;
            background-color: #0b0f19;
            border: 1px solid #334155;
            border-radius: 0.5rem;
            color: #f8fafc;
            font-size: 0.875rem;
            outline: none;
            transition: border-color 0.15s, box-shadow 0.15s;
        }
        .form-input:focus {
            border-color: #0284c7;
            box-shadow: 0 0 0 3px rgba(2, 132, 199, 0.25);
        }
        .input-group {
            position: relative;
            display: flex;
            align-items: center;
        }
        .password-toggle-btn {
            position: absolute;
            right: 0.75rem;
            background: none;
            border: none;
            color: #64748b;
            cursor: pointer;
            padding: 0.25rem;
            display: flex;
            align-items: center;
        }
        .password-toggle-btn:hover {
            color: #94a3b8;
        }
        .btn-primary {
            width: 100%;
            padding: 0.75rem 1rem;
            background-color: #0284c7;
            color: #ffffff;
            font-weight: 600;
            font-size: 0.875rem;
            border: none;
            border-radius: 0.5rem;
            cursor: pointer;
            transition: background-color 0.15s, transform 0.1s;
            text-decoration: none;
            display: inline-block;
            text-align: center;
        }
        .btn-primary:hover {
            background-color: #0369a1;
        }
        .alert-error {
            background-color: rgba(239, 68, 68, 0.1);
            border: 1px solid rgba(239, 68, 68, 0.3);
            color: #fca5a5;
            padding: 0.75rem;
            border-radius: 0.5rem;
            font-size: 0.8125rem;
            margin-bottom: 1.25rem;
        }
        .alert-success {
            background-color: rgba(34, 197, 94, 0.1);
            border: 1px solid rgba(34, 197, 94, 0.3);
            color: #86efac;
            padding: 0.75rem;
            border-radius: 0.5rem;
            font-size: 0.8125rem;
            margin-bottom: 1.25rem;
        }
        .form-footer {
            margin-top: 1.5rem;
            text-align: center;
            font-size: 0.8125rem;
            color: #94a3b8;
        }
        .form-footer a {
            color: #38bdf8;
            text-decoration: none;
            font-weight: 500;
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
                <a href="http://localhost:3000/" class="brand-logo">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg>
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
