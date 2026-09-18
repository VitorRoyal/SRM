import React from "react";
import { useTranslation } from "react-i18next";
import {
  Badge,
  Card,
  ErrorText,
  MutedText,
  PrimaryButton,
  SectionTitle,
} from "../../../../styles/GlobalStyles";
import {
  formatDateTime,
  formatExchangeRate,
  formatMoney,
  formatPercent,
} from "../../../../utils/format";
import type { SimulationResponse } from "../../types";
import {
  DetailList,
  DetailTerm,
  DetailValue,
  HighlightBox,
  HighlightLabel,
  HighlightValue,
  SettleButtonWrapper,
  SummaryHeader,
} from "./style";

interface SimulationSummaryProps {
  result: SimulationResponse | null;
  isLoading: boolean;
  errorMessage: string | null;
  canSettle: boolean;
  isSettling: boolean;
  onSettle: () => void;
}

export const SimulationSummary: React.FC<SimulationSummaryProps> = ({
  result,
  isLoading,
  errorMessage,
  canSettle,
  isSettling,
  onSettle,
}) => {
  const { t, i18n } = useTranslation();
  const language = i18n.resolvedLanguage ?? "pt";

  const renderContent = () => {
    if (errorMessage) {
      return <ErrorText>{errorMessage}</ErrorText>;
    }
    if (!result) {
      return <MutedText>{t("operatorPanel.simulation.empty")}</MutedText>;
    }
    return (
      <>
        <HighlightBox>
          <HighlightLabel>{t("operatorPanel.simulation.paymentAmount")}</HighlightLabel>
          <HighlightValue>{formatMoney(result.paymentAmount, result.paymentCurrency, language)}</HighlightValue>
        </HighlightBox>
        <DetailList>
          <DetailTerm>{t("operatorPanel.simulation.presentValue")}</DetailTerm>
          <DetailValue>{formatMoney(result.presentValueBrl, "BRL", language)}</DetailValue>
          <DetailTerm>{t("operatorPanel.simulation.discount")}</DetailTerm>
          <DetailValue>{formatMoney(result.discountBrl, "BRL", language)}</DetailValue>
          <DetailTerm>{t("operatorPanel.simulation.term")}</DetailTerm>
          <DetailValue>{t("operatorPanel.simulation.termValue", { count: result.termInMonths })}</DetailValue>
          <DetailTerm>{t("operatorPanel.simulation.baseRate")}</DetailTerm>
          <DetailValue>{formatPercent(result.monthlyBaseRate, language)}</DetailValue>
          <DetailTerm>{t("operatorPanel.simulation.spread")}</DetailTerm>
          <DetailValue>{formatPercent(result.monthlySpread, language)}</DetailValue>
          {result.exchangeRateBrlPerUnit !== null && (
            <>
              <DetailTerm>{t("operatorPanel.simulation.exchangeRate")}</DetailTerm>
              <DetailValue>{formatExchangeRate(result.exchangeRateBrlPerUnit, language)}</DetailValue>
            </>
          )}
          {result.exchangeRateEffectiveAt !== null && (
            <>
              <DetailTerm>{t("operatorPanel.simulation.exchangeRateEffectiveAt")}</DetailTerm>
              <DetailValue>{formatDateTime(result.exchangeRateEffectiveAt, language)}</DetailValue>
            </>
          )}
        </DetailList>
      </>
    );
  };

  const settleButtonLabel = () => {
    if (isSettling) {
      return t("operatorPanel.actions.settling");
    }
    return t("operatorPanel.actions.settle");
  };

  return (
    <Card>
      <SummaryHeader>
        <SectionTitle>{t("operatorPanel.simulation.sectionTitle")}</SectionTitle>
        {isLoading && <Badge>{t("operatorPanel.simulation.calculating")}</Badge>}
      </SummaryHeader>
      {renderContent()}
      <SettleButtonWrapper>
        <PrimaryButton type="button" onClick={onSettle} disabled={!canSettle || isSettling}>
          {settleButtonLabel()}
        </PrimaryButton>
      </SettleButtonWrapper>
    </Card>
  );
};
