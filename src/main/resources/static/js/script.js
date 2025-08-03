// ==================== مدیریت تم ====================
function toggleTheme() {
    const body = document.body;
    const newTheme = body.getAttribute('data-bs-theme') === 'dark' ? 'light' : 'dark';
    body.setAttribute('data-bs-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    updateThemeIcon(newTheme);
}

function updateThemeIcon(theme) {
    console.log(theme === 'dark')
    const icon = document.getElementById('themeIcon');
    if (!icon) return;

    icon.classList.remove('fa-moon', 'fa-sun');

    if (theme === 'dark') {
        icon.classList.add('fa-sun');
    } else {
        icon.classList.add('fa-moon');
    }

    // مطمئن شو کلاس 'fas' یا 'fa' هم هست (با توجه به نسخه فونت آوسام)
    if (!icon.classList.contains('fas')) {
        icon.classList.add('fas');
    }
}

// ==================== مدیریت سایدبار ====================
function toggleSidebar() {
    const sidebar = document.querySelector('.sidebar');
    if (sidebar) {
        sidebar.classList.toggle('active');
    }
}

// ==================== مدیریت زبان ====================

function initLanguageSwitcher() {
    const languageSwitcher = document.getElementById('languageSwitcher');
    if (languageSwitcher) {
        languageSwitcher.addEventListener('change', function () {
            const selectedLang = this.value;
            fetch('/admin/change-language', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `lang=${selectedLang}`
            }).then(() => location.reload());
        });
    }
}

// ==================== مقداردهی اولیه ====================
document.addEventListener('DOMContentLoaded', () => {
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.body.setAttribute('data-bs-theme', savedTheme);
    updateThemeIcon(savedTheme);
    initLanguageSwitcher();

    const themeSwitch = document.getElementById('themeSwitch');
    if (themeSwitch) {
        themeSwitch.addEventListener('click', toggleTheme);
    }

    const sidebarToggleBtn = document.getElementById('sidebarToggleBtn');
    if (sidebarToggleBtn) {
        sidebarToggleBtn.addEventListener('click', toggleSidebar);
    }

    document.body.style.overflowY = 'hidden';
});

