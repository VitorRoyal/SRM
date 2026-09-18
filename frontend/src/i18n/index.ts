import i18next from "i18next";
import { initReactI18next } from "react-i18next";
import pt from "./locales/pt.json";
import en from "./locales/en.json";
import { isAppLanguage, resolveIntlLocale, type AppLanguage } from "../utils/locale";

const LANGUAGE_STORAGE_KEY = "srm-credit-engine.language";

const readStoredLanguage = (): AppLanguage => {
  try {
    const storedLanguage = window.localStorage.getItem(LANGUAGE_STORAGE_KEY);
    if (storedLanguage && isAppLanguage(storedLanguage)) {
      return storedLanguage;
    }
  } catch {
    return "pt";
  }
  return "pt";
};

const initialLanguage = readStoredLanguage();
document.documentElement.lang = resolveIntlLocale(initialLanguage);

i18next.use(initReactI18next).init({
  resources: {
    pt: { translation: pt },
    en: { translation: en },
  },
  lng: initialLanguage,
  fallbackLng: "pt",
  interpolation: {
    escapeValue: false,
  },
});

export const changeAppLanguage = (language: AppLanguage): void => {
  i18next.changeLanguage(language);
  document.documentElement.lang = resolveIntlLocale(language);
  try {
    window.localStorage.setItem(LANGUAGE_STORAGE_KEY, language);
  } catch {
    return;
  }
};
