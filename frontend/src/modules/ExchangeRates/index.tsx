import React, { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "react-toastify";
import {
  Card,
  ErrorText,
  Field,
  FieldError,
  Input,
  Label,
  MutedText,
  PageContainer,
  PageHeader,
  PrimaryButton,
  SectionTitle,
  Select,
  Subtitle,
  Title,
} from "../../styles/GlobalStyles";
import { CURRENCY_CODES, type CurrencyCode } from "../../types/Receivable";
import { extractApiErrorMessage, extractApiErrorStatus } from "../../utils/apiError";
import { formatDateTime, formatExchangeRate } from "../../utils/format";
import { isPositiveDecimal, normalizeRateInput } from "../../utils/money";
import { getCurrentRate, registerRate } from "./service";
import type { ExchangeRate } from "./types";
import { FormStack, Hint, RateMeta, RateValue, RatesGrid } from "./style";

type CurrentRateStatus = "loading" | "loaded" | "notFound" | "error";

const CURRENT_RATE_TOAST_ID = "exchange-rates-current";

const FOREIGN_CURRENCIES = CURRENCY_CODES.filter((currency) => currency !== "BRL");

export const ExchangeRates: React.FC = () => {
  const { t, i18n } = useTranslation();
  const language = i18n.resolvedLanguage ?? "pt";

  const [currency, setCurrency] = useState<CurrencyCode>(FOREIGN_CURRENCIES[0]);
  const [currentRate, setCurrentRate] = useState<ExchangeRate | null>(null);
  const [currentRateStatus, setCurrentRateStatus] = useState<CurrentRateStatus>("loading");
  const [refreshCounter, setRefreshCounter] = useState(0);
  const [brlPerUnit, setBrlPerUnit] = useState("");
  const [isRegistering, setIsRegistering] = useState(false);

  useEffect(() => {
    let isActive = true;

    const loadCurrentRate = async () => {
      setCurrentRateStatus("loading");
      try {
        const rate = await getCurrentRate(currency);
        if (isActive) {
          setCurrentRate(rate);
          setCurrentRateStatus("loaded");
          toast.dismiss(CURRENT_RATE_TOAST_ID);
        }
      } catch (error: unknown) {
        if (!isActive) {
          return;
        }
        setCurrentRate(null);
        if (extractApiErrorStatus(error) === 404) {
          setCurrentRateStatus("notFound");
          return;
        }
        setCurrentRateStatus("error");
        toast.error(extractApiErrorMessage(error, "exchangeRates.service.fetchError"), {
          toastId: CURRENT_RATE_TOAST_ID,
        });
      }
    };

    loadCurrentRate();
    return () => {
      isActive = false;
    };
  }, [currency, refreshCounter]);

  const normalizedBrlPerUnit = useMemo(() => normalizeRateInput(brlPerUnit), [brlPerUnit]);
  const brlPerUnitOk = useMemo(() => isPositiveDecimal(normalizedBrlPerUnit), [normalizedBrlPerUnit]);
  const showBrlPerUnitError = brlPerUnit.trim().length > 0 && !brlPerUnitOk;

  const handleCurrencyChange = (event: React.ChangeEvent<HTMLSelectElement>): void => {
    const selectedCurrency = FOREIGN_CURRENCIES.find((foreignCurrency) => foreignCurrency === event.target.value);
    if (selectedCurrency) {
      setCurrency(selectedCurrency);
    }
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!brlPerUnitOk || normalizedBrlPerUnit === null) {
      return;
    }

    setIsRegistering(true);
    try {
      const registeredRate = await registerRate({ currency, brlPerUnit: Number(normalizedBrlPerUnit) });
      if (!registeredRate) {
        return;
      }
      setBrlPerUnit("");
      setRefreshCounter((previousCounter) => previousCounter + 1);
    } finally {
      setIsRegistering(false);
    }
  };

  const renderCurrentRate = () => {
    if (currentRateStatus === "loading") {
      return <MutedText>{t("common.loading")}</MutedText>;
    }
    if (currentRateStatus === "notFound") {
      return <MutedText>{t("exchangeRates.current.notFound")}</MutedText>;
    }
    if (currentRateStatus === "error" || !currentRate) {
      return <ErrorText>{t("exchangeRates.service.fetchError")}</ErrorText>;
    }
    return (
      <>
        <RateValue>
          {t("exchangeRates.current.rateValue", {
            currency: currentRate.currency,
            rate: formatExchangeRate(currentRate.brlPerUnit, language),
          })}
        </RateValue>
        <RateMeta>
          {t("exchangeRates.current.effectiveAt", { date: formatDateTime(currentRate.effectiveAt, language) })}
        </RateMeta>
      </>
    );
  };

  const submitButtonLabel = () => {
    if (isRegistering) {
      return t("exchangeRates.form.submitting");
    }
    return t("exchangeRates.form.submit");
  };

  return (
    <PageContainer>
      <PageHeader>
        <Title>{t("exchangeRates.title")}</Title>
        <Subtitle>{t("exchangeRates.subtitle")}</Subtitle>
      </PageHeader>
      <RatesGrid>
        <Card>
          <SectionTitle>{t("exchangeRates.current.sectionTitle")}</SectionTitle>
          {renderCurrentRate()}
        </Card>
        <Card>
          <SectionTitle>{t("exchangeRates.form.sectionTitle")}</SectionTitle>
          <FormStack onSubmit={handleSubmit}>
            <Field>
              <Label>{t("exchangeRates.form.currency")}</Label>
              <Select value={currency} onChange={handleCurrencyChange}>
                {FOREIGN_CURRENCIES.map((foreignCurrency) => (
                  <option key={foreignCurrency} value={foreignCurrency}>
                    {t(`common.currencies.${foreignCurrency}`)}
                  </option>
                ))}
              </Select>
            </Field>
            <Field>
              <Label>{t("exchangeRates.form.brlPerUnit")}</Label>
              <Input
                value={brlPerUnit}
                inputMode="decimal"
                placeholder={t("exchangeRates.form.brlPerUnitPlaceholder")}
                $invalid={showBrlPerUnitError}
                onChange={(event) => setBrlPerUnit(event.target.value)}
              />
              {showBrlPerUnitError && <FieldError>{t("exchangeRates.validation.brlPerUnitInvalid")}</FieldError>}
            </Field>
            <Hint>{t("exchangeRates.form.hint")}</Hint>
            <PrimaryButton type="submit" disabled={!brlPerUnitOk || isRegistering}>
              {submitButtonLabel()}
            </PrimaryButton>
          </FormStack>
        </Card>
      </RatesGrid>
    </PageContainer>
  );
};
