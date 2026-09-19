package cat.narezany.margyt;

import java.util.Locale;

/**
 * The words the mod puts on screen, in the phone's language where it has them.
 *
 * Three languages and a handful of lines: enough that the mod does not look
 * bolted onto a Russian phone in English, and small enough to keep in one file
 * rather than in string resources -- which this build cannot add.
 */
final class Text {

    private Text() {}

    private static final boolean RU = "ru".equals(language()) || "be".equals(language());
    private static final boolean UK = "uk".equals(language());

    static final String ROW = pick("Настройки MargyT", "Налаштування MargyT", "MargyT settings");

    static final String REGION = pick("Регион", "Регіон", "Region");

    static final String CHANGE_REGION = pick(
            "Менять регион", "Змінювати регіон", "Change the region");

    static final String COUNTRY = pick("Страна", "Країна", "Country");

    static final String ACCENT = pick("Цвет TikTok", "Колір TikTok", "TikTok's colour");

    static final String ACCENT_COLOUR = pick("Акцент", "Акцент", "Accent");

    static final String ACCENT_NOTE = pick(
            "Меняет розовый, которым TikTok рисует лайки, кнопки и вкладки. "
                    + "Часть значков нарисована картинками — их цвет задаётся при сборке "
                    + "и здесь не меняется.",
            "Змінює рожевий, яким TikTok малює лайки, кнопки та вкладки. "
                    + "Частина значків намальована картинками — їхній колір задається "
                    + "під час збірки і тут не змінюється.",
            "Changes the pink TikTok draws likes, buttons and tabs with. Some icons "
                    + "are pictures rather than code; their colour is settled at build "
                    + "time and does not follow.");

    /** The bar at the foot of the screen: one line, not a paragraph. */
    static final String RESTART_PENDING = pick(
            "Изменения применятся после перезапуска",
            "Зміни застосуються після перезапуску",
            "Changes apply after a restart");

    static final String RESTART = pick("Перезапустить", "Перезапустити", "Restart");

    // ------------------------------------------------ what TikTok ships off

    static final String HIDDEN = pick("Анти A/B", "Анти A/B", "Anti A/B");

    static final String HIDDEN_NOTE = pick(
            "Функции у TikTok уже написаны, но выдаются случайной части людей. "
                    + "Здесь они включаются всем.",
            "Функції в TikTok уже написані, але видаються випадковій частині людей. "
                    + "Тут вони вмикаються всім.",
            "TikTok has written these already and hands them to a random share of "
                    + "people. Here they are switched on for everyone.");

    static final String CLOSE = pick("Понятно", "Зрозуміло", "Got it");

    static final String SAVE_STICKER = pick(
            "Скачать стикер", "Завантажити стікер", "Download the sticker");

    static final String STICKER_FAILED = pick(
            "Не получилось сохранить стикер", "Не вдалося зберегти стікер",
            "Could not save the sticker");

    static final String SAVE_AVATARS_ON = pick(
            "Кнопка на аватарках", "Кнопка на аватарках", "The button on avatars");

    static final String SAVE_STICKERS_ON = pick(
            "Кнопка на стикерах", "Кнопка на стікерах", "The button on stickers");

    static final String BADGES_ON = pick("Значки", "Значки", "Badges");

    static final String SAVE_AVATAR = pick(
            "Сохранить аватарку", "Зберегти аватарку", "Save the avatar");

    static final String SAVED = pick("Сохранено", "Збережено", "Saved");

    static final String AVATAR_NOTHING = pick(
            "Нечего сохранять — откройте аватарку сначала",
            "Нема чого зберігати — відкрийте аватарку спершу",
            "Nothing to save yet -- open an avatar first");

    static final String AVATAR_FAILED = pick(
            "Не получилось сохранить", "Не вдалося зберегти", "Could not save it");

    static final String BADGE_OWNER = pick(
            "Владелец Margy и MargyT", "Власник Margy і MargyT",
            "The owner of Margy and MargyT");

    static final String VIDEO = pick("Видео", "Відео", "Video");

    static final String THEME = pick("Тема", "Тема", "Theme");

    static final String THEME_ON = pick(
            "Своя тема", "Своя тема", "A theme of your own");

    static final String THEME_MATERIAL = pick(
            "Цвета с обоев", "Кольори зі шпалер", "Colours from the wallpaper");

    static final String THEME_TEXT = pick("Текст", "Текст", "Text");

    static final String THEME_BACKGROUND = pick("Фон", "Тло", "Background");

    static final String THEME_NOTE = pick(
            "Перекрашивается только то, что TikTok и сам перекрашивает при "
                    + "смене светлой темы на тёмную. Акцент живёт отдельно.",
            "Перефарбовується лише те, що TikTok і сам перефарбовує при зміні "
                    + "світлої теми на темну. Акцент живе окремо.",
            "Only what TikTok itself repaints when you switch between light and "
                    + "dark. The accent is its own thing.");

    static final String BACKGROUND = pick(
            "Играть в фоне", "Грати у фоні", "Play in the background");

    static final String AUTOSCROLL = pick(
            "Автопрокрутка ленты", "Автопрокрутка стрічки", "Scroll the feed by itself");

    static final String SOUND = pick(
            "Звук, снятый по копирайту", "Звук, знятий за копірайтом",
            "Sound pulled for copyright");

    static final String SEEKBAR = pick(
            "Перемотка на всех видео", "Перемотка на всіх відео",
            "The scrubbing bar everywhere");

    static final String VOICE = pick(
            "Голосовые комментарии", "Голосові коментарі", "Voice comments");

    static final String THEME_STRENGTH = pick(
            "Насыщенность фона", "Насиченість тла", "How much of that background");

    static final String THEME_STRENGTH_NOTE = pick(
            "Слева — почти чёрный с оттенком выбранного цвета, справа — сам цвет.",
            "Ліворуч — майже чорний з відтінком обраного кольору, праворуч — сам колір.",
            "To the left, near black with a hint of the colour; to the right, the colour.");

    static final String ACCENT_WALLPAPER = pick(
            "Взять цвет с обоев", "Взяти колір зі шпалер", "Take the colour from the wallpaper");

    // --------------------------------------------------------------- fonts

    static final String FONT = pick("Шрифт", "Шрифт", "Typeface");

    // ----------------------------------------------------- the test builds

    static final String TEST_NOBODY = pick("не вошёл", "не увійшов", "not signed in");

    static final String TEST_TITLE = pick(
            "Тестовая сборка", "Тестова збірка", "A test build");

    static final String TEST_TEXT = pick(
            "Это тестовая сборка MargyT — она только для тех, кто поддержал "
                    + "разработку. Настройки мода откроются на аккаунте со значком "
                    + "поддержавшего; TikTok работает как обычно. Если значка нет, "
                    + "поставьте обычную версию с GitHub.",
            "Це тестова збірка MargyT — вона лише для тих, хто підтримав "
                    + "розробку. Налаштування мода відкриються на акаунті зі значком "
                    + "того, хто підтримав; TikTok працює як завжди. Якщо значка "
                    + "немає, встановіть звичайну версію з GitHub.",
            "This is a test build of MargyT, and it is for the people who paid "
                    + "for the work. The mod's settings open on an account with the "
                    + "supporter badge; TikTok itself works as usual. Without the "
                    + "badge, install the ordinary version from GitHub.");

    static final String TEST_CLOSE = pick("Понятно", "Зрозуміло", "I see");

    static final String NO_HDR = pick(
            "Убрать HDR", "Прибрати HDR", "Take the HDR off");

    static final String NO_HDR_NOTE = pick(
            "HDR-видео светят ярче всего остального и бьют по глазам ночью. "
                    + "Видео останется, лишняя яркость — нет.",
            "HDR-відео світять яскравіше за все інше й б'ють по очах уночі. "
                    + "Відео залишиться, зайва яскравість — ні.",
            "An HDR video is allowed more brightness than everything else, which "
                    + "at night is a lot. The video stays; the extra brightness does not.");

    static final String NO_HDR_OLD = pick(
            "На этом Android можно только попросить — получится не везде",
            "На цьому Android можна лише попросити — вийде не всюди",
            "On this Android it can only be asked for, and not every phone listens");

    // ------------------------------------------------- the gradient and banner

    static final String GRADIENT = pick("Градиент на ник", "Градієнт на нік", "Name gradient");

    static final String GRADIENT_NOTE = pick(
            "Ник в градиенте видят все, у кого стоит MargyT. Менять можно раз в минуту.",
            "Нік у градієнті бачать усі, у кого стоїть MargyT. Міняти можна раз на хвилину.",
            "Everyone running MargyT sees it. Changing is allowed once a minute.");

    static final String GRADIENT_ONLY = pick(
            "Только для поддержавших разработку",
            "Лише для тих, хто підтримав розробку",
            "For the people who supported the project");

    static final String GRADIENT_HUE = pick("Цвет", "Колір", "Hue");
    static final String GRADIENT_SAT = pick("Насыщенность", "Насиченість", "Saturation");
    static final String GRADIENT_VALUE = pick("Яркость", "Яскравість", "Brightness");

    static final String GRADIENT_ADD = pick("Добавить цвет", "Додати колір", "Add a colour");
    static final String GRADIENT_DROP_ONE = pick("Убрать цвет", "Прибрати колір", "Remove a colour");
    static final String GRADIENT_SAVE = pick("Сохранить градиент", "Зберегти градієнт", "Save it");
    static final String GRADIENT_OFF = pick("Убрать градиент", "Прибрати градієнт", "Take it off");

    static final String GRADIENT_SAVED = pick("Готово", "Готово", "Done");
    static final String GRADIENT_REFUSED = pick(
            "Сервер отказал. Раз в минуту, и только для поддержавших.",
            "Сервер відмовив. Раз на хвилину, і лише для тих, хто підтримав.",
            "The server said no. Once a minute, and supporters only.");
    static final String GRADIENT_NO_ACCOUNT = pick(
            "Сначала войди в аккаунт", "Спочатку увійди в акаунт", "Sign in first");

    static final String TAGS = pick("Фильтр хештегов", "Фільтр хештегів", "Hashtag filter");

    static final String TAGS_NOTE = pick(
            "Посты с этими хештегами не попадут в рекомендации. Свои посты не прячутся.",
            "Пости з цими хештегами не потраплять у рекомендації. Свої пости не ховаються.",
            "Posts with these hashtags stay out of the feed. Your own are never hidden.");

    static final String TAGS_ADD = pick("Добавить хештег", "Додати хештег", "Add a hashtag");
    static final String TAGS_NONE = pick("Пока пусто", "Поки порожньо", "None yet");
    static final String TAGS_FULL = pick("Больше не влезет", "Більше не влізе", "That is enough of those");

    static final String BANNER = pick("Баннер профиля", "Банер профілю", "Profile banner");

    static final String BANNER_NOTE = pick(
            "Картинка сверху профиля, её видят все с MargyT. До 5 МБ, менять раз в пять минут.",
            "Картинка зверху профілю, її бачать усі з MargyT. До 5 МБ, міняти раз на п'ять хвилин.",
            "A picture across the top of your profile, seen by everyone on MargyT. "
                    + "5 MB at most, changed once every five minutes.");

    static final String BANNER_RULES = pick(
            "За неприемлемую картинку баннер снимается. Во второй раз снимается значок поддержавшего.",
            "За неприйнятну картинку банер знімається. Удруге знімається значок того, хто підтримав.",
            "An unacceptable picture costs you the banner. The second time it costs "
                    + "you the supporter badge.");

    static final String BANNER_DIM = pick(
            "Затемнение баннера", "Затемнення банера", "Darken the banner");

    static final String BANNER_DIM_NOTE = pick(
            "Так его увидят все. Хранится вместе с картинкой.",
            "Так його побачать усі. Зберігається разом із картинкою.",
            "Everyone sees it this dark. Kept with the picture.");

    static final String BANNER_SHADE = pick(
            "Тень под текстом", "Тінь під текстом", "Shadow under the text");

    static final String BANNER_SHADE_NOTE = pick(
            "Чтобы ник и подписи читались на любой картинке.",
            "Щоб нік і підписи читалися на будь-якій картинці.",
            "So the name and the numbers stay readable over any picture.");

    static final String BANNER_PICK = pick("Выбрать картинку", "Вибрати картинку", "Choose a picture");
    static final String BANNER_OFF = pick("Убрать баннер", "Прибрати банер", "Take it off");
    static final String BANNER_TOO_BIG = pick(
            "Больше 5 МБ", "Більше 5 МБ", "Over 5 MB");
    static final String BANNER_UNREADABLE = pick(
            "Не смог прочитать файл", "Не зміг прочитати файл", "Could not read that file");
    static final String BANNER_REFUSED = pick(
            "Сервер отказал. Раз в пять минут, и только для поддержавших.",
            "Сервер відмовив. Раз на п'ять хвилин, і лише для тих, хто підтримав.",
            "The server said no. Once every five minutes, and supporters only.");

    static final String FPS = pick("Частота кадров", "Частота кадрів", "Frame rate");

    static final String FPS_ABOUT = pick(
            "TikTok сам решает, сколько кадров показывать: где-то 60, где-то 120.",
            "TikTok сам вирішує, скільки кадрів показувати: десь 60, десь 120.",
            "TikTok decides how many frames to show: 60 here, 120 there.");

    static final String FPS_AUTO = pick(
            "Пусть решает TikTok", "Хай вирішує TikTok", "Let TikTok decide");

    static final String FPS_LOCKED = pick("Всегда %s", "Завжди %s", "Always %s");

    static final String FPS_UNSUPPORTED = pick(
            "Экран столько не умеет — будет ближайшая",
            "Екран стільки не вміє — буде найближча",
            "The screen cannot do that, so the nearest one instead");

    static final String FONT_SYSTEM = pick("Системный", "Системний", "The system one");
    static final String FONT_SANS = pick("Обычный", "Звичайний", "Sans");
    static final String FONT_SANS_LIGHT = pick("Тонкий", "Тонкий", "Light");
    static final String FONT_SANS_CONDENSED = pick("Узкий", "Вузький", "Condensed");
    static final String FONT_SERIF = pick("С засечками", "Із засічками", "Serif");
    static final String FONT_MONOSPACE = pick("Моноширинный", "Моноширинний", "Monospace");
    static final String FONT_CURSIVE = pick("Рукописный", "Рукописний", "Cursive");
    static final String FONT_FILE = pick("Свой файл", "Свій файл", "A file of your own");

    static final String FONT_PICK = pick(
            "Выбрать .ttf или .otf", "Обрати .ttf або .otf", "Pick a .ttf or .otf");

    static final String FONT_FAILED = pick(
            "Не получилось прочитать шрифт", "Не вдалося прочитати шрифт",
            "That file is not a font this phone can read");

    static final String EMOJI = pick("Шрифт эмодзи", "Шрифт емодзі", "Emoji");

    static final String EMOJI_SYSTEM = pick("Системные", "Системні", "The system ones");
    static final String EMOJI_TWEMOJI = pick("Twemoji", "Twemoji", "Twemoji");
    static final String EMOJI_NOTO = pick("Noto", "Noto", "Noto");
    static final String EMOJI_BLOB = pick("Blobmoji", "Blobmoji", "Blobmoji");
    static final String EMOJI_FILE = pick("Свой файл", "Свій файл", "A file of your own");

    static final String EMOJI_NOTE = pick(
            "Все паки уже внутри мода, скачивать ничего не нужно. Нужен Android 10 "
                    + "и выше: ниже него меняются только буквы.",
            "Усі паки вже всередині мода, завантажувати нічого не треба. Потрібен "
                    + "Android 10 і вище: нижче змінюються лише літери.",
            "Every pack is already inside the mod; nothing is fetched. Android 10 "
                    + "and up: below that only the letters change.");

    static final String EMOJI_READY = pick(
            "Готово — перезапусти приложение", "Готово — перезапусти застосунок",
            "Done -- restart the app");

    // ---------------------------------------------------------- the icon

    static final String ICON = pick("Иконка", "Іконка", "The icon");

    static final String ICON_NOTE = pick(
            "Иконки с конкурса. Хочешь, чтобы твоя была здесь — участвуй.",
            "Іконки з конкурсу. Хочеш, щоб твоя була тут — бери участь.",
            "These came from a contest. Enter it if you want yours here.");

    static final String ICON_CONTEST = pick(
            "Участвовать в конкурсе", "Взяти участь у конкурсі", "Enter the contest");

    static final String ICON_DEFAULT = pick("Обычная", "Звичайна", "The usual one");

    // -------------------------------------------------------- the donation

    static final String DONATE_BANNER = pick(
            "Значок за поддержку", "Значок за підтримку", "A badge for supporting");

    static final String DONATE_BANNER_TEXT = pick(
            "От 250 ₽: значок рядом с ником, градиент на ник, баннер в профиле "
                    + "и закрытый чат. Значок, градиент и баннер видят все с MargyT.",
            "Від 250 ₽: значок поруч із ніком, градієнт на нік, банер у профілі "
                    + "та закритий чат. Значок, градієнт і банер бачать усі з MargyT.",
            "From 250 roubles: a badge beside your name, a gradient on the name, "
                    + "a banner on your profile and the private chat. The badge, "
                    + "the gradient and the banner are seen by everyone on MargyT.");

    static final String DONATE_BANNER_HOW = pick(
            "После перевода — @narezany в Telegram: чек и ваш ID аккаунта.",
            "Після переказу — @narezany в Telegram: чек і ваш ID акаунта.",
            "After paying, write to @narezany on Telegram with the receipt and "
                    + "your account ID.");

    static final String DONATE_BANNER_BUTTON = pick(
            "Пожертвовать", "Пожертвувати", "Donate");

    static final String DONATE_BANNER_WRITE = pick(
            "Написать в Telegram", "Написати в Telegram", "Write on Telegram");

    static final String REMIND_TITLE = pick(
            "Нравится MargyT?", "Подобається MargyT?", "Enjoying MargyT?");

    static final String REMIND_TEXT = pick(
            "Поддержи разработку — значок рядом с ником, градиент на ник, "
                    + "баннер в профиле и закрытый чат.",
            "Підтримай розробку — значок поруч із ніком, градієнт на нік, "
                    + "банер у профілі та закритий чат.",
            "Support the making of it: a badge beside your name, a gradient on "
                    + "the name, a banner on your profile and the private chat.");

    static final String REMIND_MORE = pick("Подробнее", "Докладніше", "Tell me more");

    static final String REMIND_NEVER = pick(
            "Больше не напоминать", "Більше не нагадувати", "Never remind me");

    static final String STREAK_TEST = pick(
            "Отправить тестовое сообщение", "Надіслати тестове повідомлення",
            "Send a test message");

    static final String STREAK_TEST_NOTE = pick(
            "Всем, у кого есть серия — даже если огонёк горит. Результат в дневнике.",
            "Усім, у кого є серія — навіть якщо вогник горить. Результат у щоденнику.",
            "To everyone with a streak, lit or not. The diary says what happened.");

    static final String STREAK_TEST_GOING = pick(
            "Отправляю, смотри дневник", "Надсилаю, дивись щоденник",
            "Sending; the diary will say");

    static final String CAT_FROM = pick("Принёс", "Приніс", "Brought by");

    static final String CAT_YOURS = pick(
            "Хочешь своего кота здесь? Напиши @narezany в Telegram",
            "Хочеш свого кота тут? Напиши @narezany в Telegram",
            "Want your cat here? Write to @narezany on Telegram");

    static final String CAT_WAIT = pick(
            "Коты ещё едут, попробуй ещё раз", "Коти ще їдуть, спробуй ще раз",
            "The cats are still on their way; try again");

    static final String HIDE_LIVE = pick(
            "Скрыть трансляции", "Сховати трансляції", "Hide live rooms");

    static final String HIDE_PHOTOS = pick(
            "Скрыть фото-посты", "Сховати фото-пости", "Hide slideshows");

    static final String DIM = pick(
            "Анти-выгорание", "Анти-вигоряння", "Stop the screen burning in");

    static final String DIM_HOW = pick(
            "Насколько приглушить", "Наскільки приглушити", "How far down");

    static final String DIM_NOTE = pick(
            "Приглушает кнопки, подпись и перемотку поверх видео — они стоят на "
                    + "месте часами и выжигаются в экран. Нажимаются они так же.",
            "Приглушує кнопки, підпис і перемотку поверх відео — вони стоять на "
                    + "місці годинами й випалюються в екран. Натискаються так само.",
            "Turns down the buttons, the caption and the scrubbing bar over the "
                    + "video: they sit in one place for hours and wear the panel. "
                    + "They still work exactly as they did.");

    // ------------------------------------------------------ the texture packs

    static final String TEXTURES = pick("Текстурпаки", "Текстурпаки", "Texture packs");

    static final String TEXTURES_ON = pick(
            "Включить текстурпак", "Увімкнути текстурпак", "Use a texture pack");

    static final String TEXTURES_EXPORT = pick(
            "Выгрузить картинки", "Вивантажити картинки", "Write the pictures out");

    static final String TEXTURES_EXPORT_XML = pick(
            "Выгрузить вместе с xml", "Вивантажити разом з xml",
            "Write them out with the xml");

    static final String TEXTURES_EXPORT_XML_NOTE = pick(
            "Тяжелее и почти всё там не правится руками",
            "Важче, і майже все там не редагується руками",
            "Heavier, and most of it cannot be edited by hand");

    static final String TEXTURES_EXPORT_NOTE = pick(
            "Файл .margytex со всеми картинками сборки, в Downloads/MargyT",
            "Файл .margytex з усіма картинками збірки, у Downloads/MargyT",
            "A .margytex of every picture in this build, into Downloads/MargyT");

    static final String TEXTURES_EXPORTING = pick(
            "Собираю текстуры", "Збираю текстури", "Writing the textures out");

    static final String TEXTURES_EXPORTED = pick(
            "Готово, файл в Downloads/MargyT", "Готово, файл у Downloads/MargyT",
            "Done -- the file is in Downloads/MargyT");

    static final String TEXTURES_INSTALL = pick(
            "Загрузить текстурпак", "Завантажити текстурпак", "Add a texture pack");

    static final String TEXTURES_INSTALL_NOTE = pick(
            "файл .margytex с папкой res внутри", "файл .margytex з текою res усередині",
            "a .margytex with a res folder in it");

    static final String TEXTURES_FAILED = pick(
            "В этом файле нет картинок из res", "У цьому файлі немає картинок з res",
            "That file has nothing under res in it");

    static final String TEXTURES_DOCS = pick(
            "Как сделать текстурпак", "Як зробити текстурпак",
            "How to make a texture pack");

    static final String TEXTURES_DOCS_NOTE = pick(
            "Документация на GitHub", "Документація на GitHub",
            "The documentation on GitHub");

    static final String TEXTURES_WRONG_VERSION = pick(
            "Для другой версии TikTok", "Для іншої версії TikTok",
            "For a different TikTok");

    static final String TEXTURES_NONE = pick(
            "Пока ни одного", "Поки жодного", "None yet");

    static final String TEXTURES_NOTE = pick(
            "Пак сделан под конкретную версию TikTok. На другой версии часть "
                    + "картинок просто не подставится — ничего не сломается.",
            "Пак зроблено під конкретну версію TikTok. На іншій версії частина "
                    + "картинок просто не підставиться — нічого не зламається.",
            "A pack is drawn against one version of TikTok. On another, some "
                    + "pictures simply are not swapped -- nothing breaks.");

    // ------------------------------------------------------------ own badges

    static final String MINE = pick("Мои значки", "Мої значки", "My badges");

    static final String MINE_NOTE = pick(
            "Порядок и что показывать. Сохраняется на сервере, видно всем.",
            "Порядок і що показувати. Зберігається на сервері, видно всім.",
            "The order, and which to show. Kept on the server, seen by everyone.");

    static final String MINE_NONE = pick(
            "У этого аккаунта пока нет значков", "У цього акаунта поки немає значків",
            "This account has no badges yet");

    static final String MINE_WAIT = pick("Спрашиваю сервер…", "Питаю сервер…",
            "Asking the server...");

    static final String MINE_SAVE = pick(
            "Сохранить значки", "Зберегти значки", "Save my badges");

    static final String MINE_SAVED = pick("Сохранено", "Збережено", "Saved");

    static final String MINE_TOO_OFTEN = pick(
            "Не чаще раза в минуту", "Не частіше разу на хвилину",
            "Once a minute at most");

    static final String FREE_BADGE = pick(
            "Бесплатный значок олда!", "Безкоштовний значок олда!",
            "A free badge for the old lot");

    static final String FREE_BADGE_TEXT = pick(
            "Бирюзовый значок «Использовал MargyT до 20-го сентября». "
                    + "После 20-го его больше не выдадут — ни за что.",
            "Бірюзовий значок «Користувався MargyT до 20-го вересня». "
                    + "Після 20-го його більше не видадуть — ні за що.",
            "A turquoise badge that says you were here before the 20th of "
                    + "September. After that nobody gets one, at any price.");

    static final String FREE_BADGE_TAKE = pick("Забрать", "Забрати", "Take it");

    static final String FREE_BADGE_GOT = pick(
            "Значок твой", "Значок твій", "It is yours");

    static final String ALWAYS_DATE = pick(
            "Дата под каждым видео", "Дата під кожним відео",
            "The date on every video");

    static final String ALWAYS_DATE_NOTE = pick(
            "Тикток показывает её только если открыть видео с профиля автора.",
            "Тікток показує її лише якщо відкрити відео з профілю автора.",
            "TikTok shows it only when the video was opened from a profile.");

    // ------------------------------------------------------ the plugin store

    static final String STORE = pick("Магазин плагинов", "Магазин плагінів",
            "The plugin store");

    static final String STORE_OPEN = pick("Открыть магазин", "Відкрити магазин",
            "Open the store");

    static final String STORE_WAIT = pick("Смотрю, что есть…", "Дивлюсь, що є…",
            "Looking...");

    static final String STORE_OFFLINE = pick(
            "Магазин не отвечает", "Магазин не відповідає", "The store is not answering");

    static final String STORE_GET = pick("Поставить", "Поставити", "Get it");
    static final String STORE_ANYWAY = pick("Всё равно", "Все одно", "Anyway");
    static final String STORE_GETTING = pick("Качаю плагин", "Завантажую плагін",
            "Fetching the plugin");
    static final String STORE_GOT = pick("Готово, перезапусти приложение",
            "Готово, перезапусти застосунок", "Done -- restart the app");
    static final String STORE_FAILED = pick("Не вышло", "Не вийшло", "That did not work");

    static final String STORE_WRONG_VERSION = pick(
            "Для другой версии TikTok", "Для іншої версії TikTok",
            "For a different TikTok");

    static final String STORE_NOTE = pick(
            "Плагины загружает владелец мода. Ставь только то, чему доверяешь: "
                    + "плагин работает внутри тиктока с твоим аккаунтом.",
            "Плагіни завантажує власник мода. Став лише те, чому довіряєш: "
                    + "плагін працює всередині тіктока з твоїм акаунтом.",
            "Plugins are put there by whoever runs the mod. Install what you trust: "
                    + "a plugin runs inside TikTok with your account.");

    static final String SAVE = pick("Сохранить", "Зберегти", "Save");

    // -------------------------------------------------------- the downloads

    static final String DOWNLOADS = pick("Скачивание", "Завантаження", "Downloads");

    static final String NO_WATERMARK = pick(
            "Без водяного знака", "Без водяного знака", "Without the watermark");

    static final String DOWNLOAD_ALWAYS = pick(
            "Сохранять можно всё", "Зберігати можна все", "Save anything");

    static final String FEED = pick("Лента", "Стрічка", "Feed");

    static final String HIDE_ADS = pick(
            "Убирать рекламу", "Прибирати рекламу", "Drop the advertisements");

    // --------------------------------------------------------- the plugins

    static final String PLUGINS = pick("Плагины", "Плагіни", "Plugins");

    static final String PLUGIN_INSTALL = pick(
            "Установить плагин", "Встановити плагін", "Install a plugin");

    static final String PLUGIN_INSTALL_NOTE = pick(
            "Файл .mtp", "Файл .mtp", "An .mtp file");

    static final String PLUGIN_NONE = pick(
            "Пока ничего не установлено.",
            "Поки нічого не встановлено.",
            "Nothing installed yet.");

    static final String PLUGIN_WARNING = pick(
            "Песочницы нет. Ставьте только то, чему доверяете.",
            "Пісочниці немає. Встановлюйте лише те, чому довіряєте.",
            "No sandbox. Install only what you trust.");

    static final String PLUGIN_DOCS = pick(
            "Как писать плагины", "Як писати плагіни", "Writing plugins");

    static final String PLUGIN_DOCS_NOTE = pick(
            "Документация на GitHub", "Документація на GitHub", "The documentation on GitHub");

    static final String PLUGIN_INSTALLED = pick(
            "Плагин установлен", "Плагін встановлено", "Plugin installed");

    static final String PLUGIN_REMOVE = pick("Удалить", "Видалити", "Remove");

    static final String PLUGIN_REMOVE_ASK = pick(
            "Удалить плагин?", "Видалити плагін?", "Remove the plugin?");

    static final String CANCEL = pick("Отмена", "Скасувати", "Cancel");

    static final String PLUGIN_HOLD = pick(
            "Долгое нажатие — удалить",
            "Довге натискання — видалити",
            "Hold to remove");

    // ----------------------------------------------------------- the links

    static final String LINKS = pick("Ссылки", "Посилання", "Links");

    static final String CHANNEL = pick("Канал", "Канал", "Channel");

    static final String FORUM = pick("Форум", "Форум", "Forum");

    // ----------------------------------------------------------- the streaks

    static final String STREAKS = pick("Серии", "Серії", "Streaks");

    static final String STREAK_AUTO = pick(
            "Продлевать серии сами", "Продовжувати серії самі", "Keep streaks alive");

    static final String BETA = pick("бета", "бета", "beta");

    static final String STREAK_NOTE = pick(
            "Отправляет выбранный стикер тем, с кем серия вот-вот погаснет. "
                    + "Не чаще раза в сутки на человека.",
            "Надсилає вибраний стікер тим, з ким серія ось-ось згасне. "
                    + "Не частіше разу на добу на людину.",
            "Sends the sticker you picked to whoever the streak is about to lapse "
                    + "with, at most once a day each.");

    static final String STREAK_STICKER = pick(
            "Чем продлевать", "Чим продовжувати", "What to send");

    static final String STREAK_NOTHING = pick(
            "Откройте стикеры в переписке, и они появятся здесь",
            "Відкрийте стікери в листуванні, і вони з'являться тут",
            "Open the stickers in a chat and they will show up here");

    // ------------------------------------------------------- the updates

    static final String UPDATE = pick("Обновление", "Оновлення", "An update");

    static final String UPDATE_THERE_IS = pick(
            "Вышла версия", "Вийшла версія", "There is a version");

    static final String UPDATE_GET = pick("Скачать", "Завантажити", "Get it");

    static final String UPDATE_LATER = pick("Не сейчас", "Не зараз", "Not now");

    static final String UPDATE_NEVER = pick(
            "Больше не напоминать", "Більше не нагадувати", "Stop reminding me");

    static final String UPDATE_GETTING = pick("Скачиваю", "Завантажую", "Getting it");

    static final String UPDATE_FAILED = pick(
            "Не получилось скачать", "Не вдалося завантажити", "Could not get it");

    static final String UPDATE_NONE = pick(
            "Уже последняя версия", "Вже остання версія", "This is the latest");

    static final String UPDATE_NO_ANSWER = pick(
            "GitHub не ответил", "GitHub не відповів", "GitHub did not answer");

    static final String UPDATE_ALLOW = pick(
            "Разрешите установку из этого источника, и я поставлю",
            "Дозвольте встановлення з цього джерела, і я поставлю",
            "Allow installing from this source and it will go on");

    static final String UPDATE_CHECK = pick(
            "Проверить обновления", "Перевірити оновлення", "Check for updates");

    static final String UPDATE_REMIND = pick(
            "Напоминать об обновлениях", "Нагадувати про оновлення",
            "Remind me about updates");

    static final String UPDATE_INSTALL = pick(
            "Установить скачанное", "Встановити завантажене", "Install what was downloaded");

    static final String VERSIONS = pick("Версии", "Версії", "Versions");

    static final String SOURCE = pick("Исходники", "Вихідники", "The source");

    static final String THANKS = pick("Благодарности", "Подяки", "Thanks");

    static final String THANKS_NOTE = pick(
            "Люди, без которых мода бы не было.",
            "Люди, без яких мода б не було.",
            "The people the mod would not exist without.");

    static final String THANKS_OWNER = pick(
            "Владелец мода", "Власник мода", "The mod's owner");

    static final String THANKS_CLAUDE = pick(
            "Написал большую часть того, что в моде есть",
            "Написав більшу частину того, що в моді є",
            "Wrote most of what is in the mod");

    static final String THANKS_HELPER = pick(
            "Помог с несколькими фичами",
            "Допоміг з кількома фічами",
            "Helped with several of the features");

    static final String DONATE = pick("Поддержать", "Підтримати", "Support the project");

    static final String DONATE_NOTE = pick(
            "Каждый ваш рубль помогает держать проект на плаву и развивать открытое "
                    + "сообщество моддинга. Спасибо.",
            "Кожен ваш рубль допомагає тримати проєкт на плаву та розвивати відкриту "
                    + "спільноту модингу. Дякуємо.",
            "Every rouble keeps the project afloat and goes back into the open source "
                    + "modding community. Thank you.");

    static final String CARD = pick("Карта", "Картка", "Card");

    static final String TAP_TO_COPY = pick(
            "Нажми, чтобы скопировать", "Натисни, щоб скопіювати", "Tap to copy");

    static final String YOOMONEY = pick("ЮMoney", "ЮMoney", "YooMoney");

    static final String YOOMONEY_NOTE = pick(
            "Перевод напрямую", "Переказ напряму", "Straight to the transfer page");

    static final String NO_BROWSER = pick(
            "Нечем открыть ссылку", "Нема чим відкрити посилання", "Nothing here opens links");

    static final String DIARY_TITLE = pick("Журнал мода", "Журнал мода", "The mod's diary");

    static final String COPY = pick("Скопировать", "Скопіювати", "Copy");

    static final String COPIED = pick("Скопировано", "Скопійовано", "Copied");

    static final String CLEAR = pick("Очистить", "Очистити", "Clear");

    static final String DIARY = pick(
            "Что мод видел", "Що мод бачив", "What the mod saw");

    static final String CLEARED = pick("Журнал очищен", "Журнал очищено", "The diary is empty");

    static final String ACCOUNT = pick("Аккаунт", "Акаунт", "Account");

    static final String ACCOUNT_ID = pick("ID", "ID", "ID");

    static final String ACCOUNT_SEC_ID = pick(
            "ID для ссылки", "ID для посилання", "The id a link is built from");

    static final String ACCOUNT_UNKNOWN = pick(
            "Пока неизвестен", "Поки невідомий", "Not seen yet");

    private static String language() {
        try {
            return Locale.getDefault().getLanguage();
        } catch (Throwable ignored) {
            return "en";
        }
    }

    private static String pick(String russian, String ukrainian, String english) {
        if (RU) return russian;
        if (UK) return ukrainian;
        return english;
    }
}
