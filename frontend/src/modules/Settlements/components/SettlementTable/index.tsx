import React from "react";
import { useTranslation } from "react-i18next";
import { Badge } from "../../../../styles/GlobalStyles";
import { formatDateTime, formatExchangeRate, formatIsoDate, formatMoney } from "../../../../utils/format";
import type { SettlementRow } from "../../types";
import {
  BodyCell,
  BodyRow,
  EmptyCell,
  HeaderCell,
  SecondaryLine,
  Table,
  TableWrapper,
} from "./style";

interface SettlementTableProps {
  settlements: SettlementRow[];
}

const COLUMN_COUNT = 9;

export const SettlementTable: React.FC<SettlementTableProps> = ({ settlements }) => {
  const { t, i18n } = useTranslation();
  const language = i18n.resolvedLanguage ?? "pt";

  const renderExchangeRate = (settlement: SettlementRow) => {
    if (settlement.exchangeRateBrlPerUnit === null) {
      return <Badge>{settlement.paymentCurrency}</Badge>;
    }
    return formatExchangeRate(settlement.exchangeRateBrlPerUnit, language);
  };

  return (
    <TableWrapper>
      <Table>
        <thead>
          <tr>
            <HeaderCell>{t("settlements.table.settledAt")}</HeaderCell>
            <HeaderCell>{t("settlements.table.assignor")}</HeaderCell>
            <HeaderCell>{t("settlements.table.receivableType")}</HeaderCell>
            <HeaderCell $numeric>{t("settlements.table.faceValue")}</HeaderCell>
            <HeaderCell $numeric>{t("settlements.table.term")}</HeaderCell>
            <HeaderCell $numeric>{t("settlements.table.presentValue")}</HeaderCell>
            <HeaderCell $numeric>{t("settlements.table.discount")}</HeaderCell>
            <HeaderCell $numeric>{t("settlements.table.paymentAmount")}</HeaderCell>
            <HeaderCell $numeric>{t("settlements.table.exchangeRate")}</HeaderCell>
          </tr>
        </thead>
        <tbody>
          {settlements.length === 0 && (
            <tr>
              <EmptyCell colSpan={COLUMN_COUNT}>{t("settlements.table.empty")}</EmptyCell>
            </tr>
          )}
          {settlements.map((settlement) => (
            <BodyRow key={settlement.id}>
              <BodyCell>{formatDateTime(settlement.settledAt, language)}</BodyCell>
              <BodyCell>
                {settlement.assignorName}
                <SecondaryLine>{settlement.documentNumber}</SecondaryLine>
              </BodyCell>
              <BodyCell>
                {t(`common.receivableTypes.${settlement.receivableType}`)}
                <SecondaryLine>{formatIsoDate(settlement.dueDate, language)}</SecondaryLine>
              </BodyCell>
              <BodyCell $numeric>{formatMoney(settlement.faceValue, "BRL", language)}</BodyCell>
              <BodyCell $numeric>{t("settlements.table.termValue", { count: settlement.termInMonths })}</BodyCell>
              <BodyCell $numeric>{formatMoney(settlement.presentValueBrl, "BRL", language)}</BodyCell>
              <BodyCell $numeric>{formatMoney(settlement.discountBrl, "BRL", language)}</BodyCell>
              <BodyCell $numeric $emphasis>
                {formatMoney(settlement.paymentAmount, settlement.paymentCurrency, language)}
              </BodyCell>
              <BodyCell $numeric>{renderExchangeRate(settlement)}</BodyCell>
            </BodyRow>
          ))}
        </tbody>
      </Table>
    </TableWrapper>
  );
};
