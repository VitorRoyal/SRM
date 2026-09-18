export type AppLanguage = "pt" | "en";

export const APP_LANGUAGES: AppLanguage[] = ["pt", "en"];

const INTL_LOCALE_BY_LANGUAGE: Record<AppLanguage, string> = {
  pt: "pt-BR",
  en: "en-US",
};

export const isAppLanguage = (value: string): value is AppLanguage =>
  APP_LANGUAGES.includes(value as AppLanguage);

export const resolveIntlLocale = (language: string): string => {
  if (isAppLanguage(language)) {
    return INTL_LOCALE_BY_LANGUAGE[language];
  }
  return INTL_LOCALE_BY_LANGUAGE.pt;
};
