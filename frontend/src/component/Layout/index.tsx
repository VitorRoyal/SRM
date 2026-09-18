import React from "react";
import { Outlet } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { changeAppLanguage } from "../../i18n";
import { APP_LANGUAGES, isAppLanguage } from "../../utils/locale";
import {
  Brand,
  Header,
  HeaderContent,
  LanguageField,
  LanguageSelect,
  Navigation,
  NavigationLink,
} from "./style";

export const Layout: React.FC = () => {
  const { t, i18n } = useTranslation();

  const handleLanguageChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    if (isAppLanguage(event.target.value)) {
      changeAppLanguage(event.target.value);
    }
  };

  return (
    <>
      <Header>
        <HeaderContent>
          <Brand>{t("layout.brand")}</Brand>
          <Navigation>
            <NavigationLink to="/" end>
              {t("layout.nav.operatorPanel")}
            </NavigationLink>
            <NavigationLink to="/settlements">{t("layout.nav.settlements")}</NavigationLink>
            <NavigationLink to="/exchange-rates">{t("layout.nav.exchangeRates")}</NavigationLink>
          </Navigation>
          <LanguageField>
            {t("layout.languageLabel")}
            <LanguageSelect value={i18n.resolvedLanguage} onChange={handleLanguageChange}>
              {APP_LANGUAGES.map((language) => (
                <option key={language} value={language}>
                  {t(`layout.languages.${language}`)}
                </option>
              ))}
            </LanguageSelect>
          </LanguageField>
        </HeaderContent>
      </Header>
      <Outlet />
    </>
  );
};
