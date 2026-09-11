package visualparticle.better.client.ui;

import java.util.Locale;

public enum UiLanguage {
    RU, EN;

    private static UiLanguage current = RU;

    public static UiLanguage current(){return current;}
    public static void use(String value){try{current=valueOf(value.toUpperCase(Locale.ROOT));}catch(Exception ignored){current=RU;}}
    public static String code(){return current.name();}
    public static String text(String key){return current==EN?key:ru(key);}
    public static String option(String value){return current==EN?value:ruOption(value);}

    private static String ru(String key){return switch(key){
        case "General"->"Главное"; case "Events"->"События"; case "World"->"Мир"; case "Colors"->"Цвета"; case "Presets"->"Пресеты";
        case "Master switch"->"Главный переключатель"; case "Color preset"->"Цветовой пресет"; case "Particle shape"->"Форма частиц"; case "Glow mode"->"Режим свечения";
        case "Spread"->"Разброс"; case "Speed"->"Скорость"; case "Life"->"Время жизни"; case "Size"->"Размер"; case "Glow"->"Свечение"; case "Particle physics"->"Физика частиц";
        case "Triggers"->"Триггеры"; case "Attack particles"->"Частицы атаки"; case "Totem particles"->"Частицы тотема"; case "Walk particles"->"Частицы ходьбы"; case "Elytra particles"->"Частицы элитр"; case "Projectile particles"->"Частицы снарядов";
        case "Attack amount"->"Частиц атаки"; case "Walk amount"->"Частиц ходьбы"; case "World particles"->"Частицы мира"; case "World physics"->"Физика мира"; case "World shape"->"Форма частиц мира"; case "World amount"->"Частиц мира"; case "World life"->"Жизнь частиц мира"; case "World size"->"Размер частиц мира"; case "World glow"->"Свечение частиц мира";
        case "GUI primary"->"Основной цвет GUI"; case "GUI secondary"->"Второй цвет GUI"; case "Custom particle colors"->"Свои цвета частиц"; case "Animated gradients"->"Анимированные градиенты";
        case "Attack primary"->"Основной цвет атаки"; case "Attack gradient"->"Градиент атаки"; case "Move primary"->"Основной цвет движения"; case "Move gradient"->"Градиент движения"; case "Projectile primary"->"Основной цвет снарядов"; case "Projectile gradient"->"Градиент снарядов"; case "Elytra primary"->"Основной цвет элитр"; case "Elytra gradient"->"Градиент элитр"; case "World primary"->"Основной цвет мира"; case "World gradient"->"Градиент мира"; case "Totem primary"->"Основной цвет тотема"; case "Totem gradient"->"Градиент тотема";
        case "Active preset"->"Активный пресет"; case "Search settings..."->"Поиск настроек..."; case "No settings found"->"Настройки не найдены"; case "enabled"->"включено";
        case "Enable the particle system"->"Включает всю систему частиц"; case "Built-in palette"->"Готовая цветовая палитра"; case "Particle sprite"->"Текстура частицы"; case "Bloom and trail rendering"->"Свечение и шлейф частиц"; case "Spawn spread"->"Разброс появления частиц"; case "Velocity multiplier"->"Множитель скорости"; case "Particle lifetime"->"Время жизни частицы"; case "Particle scale"->"Масштаб частицы"; case "Glow size"->"Размер свечения"; case "Gravity and block collision"->"Гравитация и столкновения с блоками";
        case "Particle sources"->"Источники появления частиц"; case "Spawn on hit"->"Появление при ударе"; case "Spawn on totem pop"->"Появление при срабатывании тотема"; case "Spawn while moving"->"Появление во время движения"; case "Spawn while flying"->"Появление во время полёта"; case "Spawn on owned projectiles"->"Появление на выпущенных снарядах"; case "Particles per hit"->"Количество частиц за удар"; case "Movement density"->"Плотность частиц движения";
        case "Ambient particles"->"Фоновые частицы мира"; case "Physics for ambient particles"->"Физика фоновых частиц"; case "Ambient particle sprite"->"Текстура частиц мира"; case "Ambient density"->"Плотность частиц мира"; case "Ambient particle lifetime"->"Время жизни частиц мира"; case "Ambient particle scale"->"Масштаб частиц мира"; case "Ambient glow size"->"Размер свечения частиц мира";
        case "Primary interface color"->"Основной цвет интерфейса"; case "Secondary interface color"->"Второй цвет интерфейса"; case "Use the colors below"->"Использовать указанные ниже цвета"; case "Animate primary and secondary colors"->"Анимировать переход между цветами"; case "Quick settings profile"->"Готовый профиль настроек";
        case "Primary attack color"->"Основной цвет частиц атаки"; case "Secondary attack color"->"Второй цвет частиц атаки"; case "Primary movement color"->"Основной цвет частиц движения"; case "Secondary movement color"->"Второй цвет частиц движения"; case "Primary projectile color"->"Основной цвет частиц снарядов"; case "Secondary projectile color"->"Второй цвет частиц снарядов"; case "Primary elytra color"->"Основной цвет частиц элитр"; case "Secondary elytra color"->"Второй цвет частиц элитр"; case "Primary world color"->"Основной цвет частиц мира"; case "Secondary world color"->"Второй цвет частиц мира"; case "Primary totem color"->"Основной цвет частиц тотема"; case "Secondary totem color"->"Второй цвет частиц тотема";
        default->key;};}

    private static String ruOption(String value){return switch(value){
        case "Default"->"Обычный"; case "Blue"->"Синий"; case "Red"->"Красный"; case "Purple"->"Фиолетовый"; case "Pink"->"Розовый"; case "Cyan"->"Циан"; case "Green"->"Зелёный"; case "Gold"->"Золотой"; case "Orange"->"Оранжевый"; case "Lime"->"Лайм"; case "Ice"->"Лёд"; case "Fire"->"Огонь"; case "Galaxy"->"Галактика"; case "White"->"Белый"; case "Random"->"Случайный";
        case "Star"->"Звезда"; case "Star Alt"->"Звезда Alt"; case "Snowflake"->"Снежинка"; case "Dollar"->"Доллар"; case "Heart"->"Сердце"; case "Line"->"Линия"; case "Lightning"->"Молния"; case "Triangle"->"Треугольник"; case "Rhombus"->"Ромб"; case "Crown"->"Корона"; case "Cross"->"Крест"; case "Arrow"->"Стрела"; case "Pearl Mark"->"Метка жемчуга"; case "Cube Blast"->"Взрыв куба"; case "Dash Bloom"->"Вспышка";
        case "Off"->"Выкл."; case "Trail"->"Шлейф"; case "Both"->"Оба"; case "Custom"->"Свой"; case "Minimal"->"Минимал"; case "Cinematic"->"Кинематик"; default->value;};}
}