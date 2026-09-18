import React from "react";
import { useTranslation } from "react-i18next";
import { Badge, Card, SectionTitle } from "../../../../styles/GlobalStyles";
import { formatDateTime, formatMoney } from "../../../../utils/format";
import type { SettlementResponse } from "../../types";
import { LastSettlementContent, LastSettlementLine, StatementLink } from "./style";

interface LastSettlementCardProps {
  settlement: SettlementResponse;
}

export const LastSettlementCard: React.FC<LastSettlementCardProps> = ({ settlement }) => {
  const { t, i18n } = useTranslation();
  const language = i18n.resolvedLanguage ?? "pt";

  return (
    <Card>
      <SectionTitle>{t("operatorPanel.lastSettlement.title")}</SectionTitle>
      <LastSettlementContent>
        <Badge $tone="accent">{settlement.id}</Badge>
        <LastSettlementLine>
          {t("operatorPanel.lastSettlement.document", {
            documentNumber: settlement.documentNumber,
            assignorName: settlement.assignorName,
          })}
        </LastSettlementLine>
        <LastSettlementLine>
          {t("operatorPanel.lastSettlement.paid", {
            amount: formatMoney(settlement.paymentAmount, settlement.paymentCurrency, language),
            date: formatDateTime(settlement.settledAt, language),
          })}
        </LastSettlementLine>
        <StatementLink to="/settlements">{t("operatorPanel.lastSettlement.viewStatement")}</StatementLink>
      </LastSettlementContent>
    </Card>
  );
};
