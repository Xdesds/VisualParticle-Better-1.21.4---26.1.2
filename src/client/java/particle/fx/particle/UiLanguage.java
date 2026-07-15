package particle.fx.particle;

import java.util.Locale;

enum UiLanguage {
	RU,
	EN;

	UiLanguage next() {
		return this == RU ? EN : RU;
	}

	String code() {
		return name();
	}

	String label() {
		return this == RU ? "Русский" : "English";
	}

	String text(String key) {
		if (this == EN) {
			return key;
		}

		return switch (key) {
			case "General" -> "Главное";
			case "Events" -> "События";
			case "World" -> "Мир";
			case "Colors" -> "Цвета";
			case "Presets" -> "Пресеты";
			case "main controls" -> "основные настройки";
			case "triggers" -> "триггеры";
			case "ambient" -> "окружение";
			case "palette" -> "палитра";
			case "profiles" -> "профили";
			case "Enabled" -> "Включено";
			case "Disabled" -> "Выключено";
			case "Save" -> "Сохранить";
			case "Reset" -> "Сброс";
			case "Done" -> "Готово";
			case "saved" -> "сохранено";
			case "autosave" -> "автосейв";
			case "Main" -> "Основное";
			case "Master switch" -> "Главный переключатель";
			case "Color preset" -> "Цветовой пресет";
			case "Particle shape" -> "Форма частиц";
			case "Glow mode" -> "Режим свечения";
			case "Tuning" -> "Настройка";
			case "Spread" -> "Разброс";
			case "Speed" -> "Скорость";
			case "Life" -> "Время жизни";
			case "Size" -> "Размер";
			case "Glow" -> "Свечение";
			case "Triggers" -> "Триггеры";
			case "Attack particles" -> "Частицы атаки";
			case "Totem particles" -> "Частицы тотема";
			case "Walk particles" -> "Частицы ходьбы";
			case "Elytra particles" -> "Частицы элитр";
			case "Projectile particles" -> "Частицы снарядов";
			case "Amounts" -> "Количество";
			case "Attack amount" -> "Частиц атаки";
			case "Walk amount" -> "Частиц ходьбы";
			case "World particles" -> "Частицы мира";
			case "World physics" -> "Физика мира";
			case "World shape" -> "Форма мира";
			case "World amount" -> "Частиц мира";
			case "World life" -> "Жизнь мира";
			case "World size" -> "Размер мира";
			case "World glow" -> "Свечение мира";
			case "Theme" -> "Тема";
			case "GUI primary" -> "GUI основной";
			case "GUI secondary" -> "GUI второй";
			case "Custom particle colors" -> "Свои цвета частиц";
			case "Animated gradients" -> "Анимированные градиенты";
			case "Particle colors" -> "Цвета частиц";
			case "Attack primary" -> "Атака основной";
			case "Attack gradient" -> "Атака градиент";
			case "Move primary" -> "Движение основной";
			case "Move gradient" -> "Движение градиент";
			case "Projectile primary" -> "Снаряд основной";
			case "Projectile gradient" -> "Снаряд градиент";
			case "Elytra primary" -> "Элитры основной";
			case "Elytra gradient" -> "Элитры градиент";
			case "World primary" -> "Мир основной";
			case "World gradient" -> "Мир градиент";
			case "Totem primary" -> "Тотем основной";
			case "Totem gradient" -> "Тотем градиент";
			case "Active preset" -> "Активный пресет";
			case "Create preset" -> "Создать пресет";
			case "Create" -> "Создать";
			case "Save selected custom" -> "Сохранить выбранный";
			case "PvP preset" -> "PvP пресет";
			case "Minimal preset" -> "Минимальный пресет";
			case "Cinematic preset" -> "Кинематик пресет";
			case "Apply" -> "Применить";
			case "Load" -> "Загрузить";
			case "Custom" -> "Свой";
			case "Minimal" -> "Минимал";
			case "Cinematic" -> "Кинематик";
			case "Create Preset" -> "Создать пресет";
			case "Cancel" -> "Отмена";
			case "Preset Name" -> "Название пресета";
			case "Preset name" -> "Название пресета";
			default -> key;
		};
	}

	String option(Enum<?> value) {
		if (this == EN) {
			return clean(value.name());
		}

		return switch (value.name()) {
			case "DEFAULT" -> "обычный";
			case "BLUE" -> "синий";
			case "RED" -> "красный";
			case "PURPLE" -> "фиолетовый";
			case "PINK" -> "розовый";
			case "CYAN" -> "циан";
			case "GREEN" -> "зеленый";
			case "GOLD" -> "золотой";
			case "ORANGE" -> "оранжевый";
			case "LIME" -> "лайм";
			case "ICE" -> "лед";
			case "FIRE" -> "огонь";
			case "GALAXY" -> "галактика";
			case "WHITE" -> "белый";
			case "RANDOM" -> "случайно";
			case "CUBES" -> "кубы";
			case "CRYSTALS" -> "кристаллы";
			case "CROWN" -> "корона";
			case "CUBE_BLAST" -> "куб взрыв";
			case "DOLLAR" -> "доллар";
			case "HEART" -> "сердце";
			case "LIGHTNING" -> "молния";
			case "LINE" -> "линия";
			case "RHOMBUS" -> "ромб";
			case "SNOWFLAKE" -> "снежинка";
			case "STAR" -> "звезда";
			case "STAR_ALT" -> "звезда alt";
			case "TRIANGLE" -> "треугольник";
			case "BLOOM" -> "bloom";
			case "BLOOM_SAMPLE" -> "bloom sample";
			case "BOTH" -> "оба";
			default -> clean(value.name());
		};
	}

	static UiLanguage fromName(String value) {
		if (value == null) {
			return RU;
		}

		try {
			return UiLanguage.valueOf(value.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ignored) {
			return RU;
		}
	}

	private static String clean(String value) {
		return value.toLowerCase(Locale.ROOT).replace('_', ' ');
	}
}
