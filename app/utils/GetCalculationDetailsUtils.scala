/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import utils.GetCalculationListUtils.calculationId

object GetCalculationDetailsUtils {

  def getCalculationDetailsSuccessResponse(lastTwoChars: String, taxYear: Option[Int]): String = {
    s"""{
       |	"metadata": {
       |		"calculationId": "${calculationId(lastTwoChars, taxYear)}",
       |		"taxYear": 2018,
       |		"requestedBy": "customer",
       |		"requestedTimestamp": "2019-02-15T09:35:15.094Z",
       |		"calculationReason": "customerRequest",
       |		"calculationTimestamp": "2019-02-15T09:35:15.094Z",
       |		"calculationType": "inYear",
       |		"intentToCrystallise": false,
       |		"crystallised": false,
       |		"crystallisationTimestamp": "2019-02-15T09:35:15.094Z",
       |		"periodFrom": "2018-01-01",
       |		"periodTo": "2019-01-01"
       |	},
       |	"inputs": {
       |		"personalInformation": {
       |			"identifier": "VO123456A",
       |			"dateOfBirth": "1988-08-27",
       |			"taxRegime": "Scotland",
       |			"statePensionAgeDate": "2053-08-27"
       |		},
       |		"incomeSources": {
       |			"businessIncomeSources": [{
       |					"incomeSourceId": "AaIS12345678910",
       |					"incomeSourceType": "01",
       |					"incomeSourceName": "Self-Employment Business ONE",
       |					"accountingPeriodStartDate": "2018-01-01",
       |					"accountingPeriodEndDate": "2019-01-01",
       |					"source": "MTD-SA",
       |					"latestPeriodEndDate": "2019-01-01",
       |					"latestReceivedDateTime": "2019-08-06T11:45:01Z",
       |					"finalised": false,
       |					"finalisationTimestamp": "2019-02-15T09:35:15.094Z",
       |					"submissionPeriods": [{
       |						"periodId": "abcdefghijk",
       |						"startDate": "2018-01-01",
       |						"endDate": "2019-01-01",
       |						"receivedDateTime": "2019-02-15T09:35:04.843Z"
       |					}]
       |				},
       |				{
       |					"incomeSourceId": "AbIS12345678910",
       |					"incomeSourceType": "01",
       |					"incomeSourceName": "Self-Employment Business TWO",
       |					"accountingPeriodStartDate": "2018-01-01",
       |					"accountingPeriodEndDate": "2019-01-01",
       |					"source": "MTD-SA",
       |					"latestPeriodEndDate": "2019-01-01",
       |					"latestReceivedDateTime": "2019-08-06T11:45:01Z",
       |					"finalised": false,
       |					"finalisationTimestamp": "2019-02-15T09:35:15.094Z",
       |					"submissionPeriods": [{
       |						"periodId": "abcdefghijk",
       |						"startDate": "2018-01-01",
       |						"endDate": "2019-01-01",
       |						"receivedDateTime": "2019-02-15T09:35:04.843Z"
       |					}]
       |				},
       |				{
       |					"incomeSourceId": "AcIS12345678910",
       |					"incomeSourceType": "02",
       |					"incomeSourceName": "UK Property Non-FHL",
       |					"accountingPeriodStartDate": "2018-01-01",
       |					"accountingPeriodEndDate": "2019-01-01",
       |					"source": "MTD-SA",
       |					"latestPeriodEndDate": "2019-01-01",
       |					"latestReceivedDateTime": "2019-08-06T11:45:01Z",
       |					"finalised": false,
       |					"finalisationTimestamp": "2019-02-15T09:35:15.094Z",
       |					"submissionPeriods": [{
       |						"periodId": "abcdefghijk",
       |						"startDate": "2018-01-01",
       |						"endDate": "2019-01-01",
       |						"receivedDateTime": "2019-02-15T09:35:04.843Z"
       |					}]
       |				},
       |				{
       |					"incomeSourceId": "AdIS12345678910",
       |					"incomeSourceType": "04",
       |					"incomeSourceName": "UK Property FHL",
       |					"accountingPeriodStartDate": "2018-01-01",
       |					"accountingPeriodEndDate": "2019-01-01",
       |					"source": "MTD-SA",
       |					"latestPeriodEndDate": "2019-01-01",
       |					"latestReceivedDateTime": "2019-08-06T11:45:01Z",
       |					"finalised": false,
       |					"finalisationTimestamp": "2019-02-15T09:35:15.094Z",
       |					"submissionPeriods": [{
       |						"periodId": "abcdefghijk",
       |						"startDate": "2018-01-01",
       |						"endDate": "2019-01-01",
       |						"receivedDateTime": "2019-02-15T09:35:04.843Z"
       |					}]
       |				}
       |			],
       |			"nonBusinessIncomeSources": [{
       |					"incomeSourceId": "SAVKB1UVwUTBQGJ",
       |					"incomeSourceType": "09",
       |					"incomeSourceName": "UK Savings Account ONE",
       |					"startDate": "2018-01-01",
       |					"endDate": "2019-01-01",
       |					"source": "MTD-SA",
       |					"periodId": "001",
       |					"latestReceivedDateTime": "2019-08-06T11:45:01Z"
       |				},
       |				{
       |					"incomeSourceId": "SAVKB2UVwUTBQGJ",
       |					"incomeSourceType": "09",
       |					"incomeSourceName": "UK Savings Account TWO",
       |					"startDate": "2018-01-01",
       |					"endDate": "2019-01-01",
       |					"source": "MTD-SA",
       |					"periodId": "001",
       |					"latestReceivedDateTime": "2019-08-06T11:45:01Z"
       |				},
       |				{
       |					"incomeSourceId": "DDIS12345678910",
       |					"incomeSourceType": "10",
       |					"incomeSourceName": "UK Dividends",
       |					"startDate": "2018-01-01",
       |					"endDate": "2019-01-01",
       |					"source": "MTD-SA",
       |					"periodId": "001",
       |					"latestReceivedDateTime": "2019-08-06T11:45:01Z"
       |				}
       |			]
       |		},
       |		"annualAdjustments": [{
       |				"incomeSourceId": "AaIS12345678910",
       |				"incomeSourceType": "01",
       |				"ascId": "10000001",
       |				"receivedDateTime": "2019-07-17T08:15:28Z",
       |				"applied": false
       |			},
       |			{
       |				"incomeSourceId": "AbIS12345678910",
       |				"incomeSourceType": "01",
       |				"ascId": "10000002",
       |				"receivedDateTime": "2019-07-17T08:15:28Z",
       |				"applied": true
       |			},
       |			{
       |				"incomeSourceId": "AcIS12345678910",
       |				"incomeSourceType": "02",
       |				"ascId": "10000003",
       |				"receivedDateTime": "2019-07-17T08:15:28Z",
       |				"applied": false
       |			},
       |			{
       |				"incomeSourceId": "AdIS12345678910",
       |				"incomeSourceType": "04",
       |				"ascId": "10000004",
       |				"receivedDateTime": "2019-07-17T08:15:28Z",
       |				"applied": true
       |			}
       |		],
       |		"lossesBroughtForward": [{
       |				"lossId": "LLIS12345678901",
       |				"incomeSourceId": "AaIS12345678910",
       |				"incomeSourceType": "01",
       |				"submissionTimestamp": "2019-07-13T07:51:43Z",
       |				"lossType": "income",
       |				"taxYearLossIncurred": 2018,
       |				"currentLossValue": 10101,
       |				"mtdLoss": true
       |			},
       |			{
       |				"lossId": "LLIS12345678902",
       |				"incomeSourceId": "AbIS12345678910",
       |				"incomeSourceType": "01",
       |				"submissionTimestamp": "2019-07-13T07:51:43Z",
       |				"lossType": "income",
       |				"taxYearLossIncurred": 2018,
       |				"currentLossValue": 10102,
       |				"mtdLoss": true
       |			},
       |			{
       |				"lossId": "LLIS12345678903",
       |				"incomeSourceId": "AcIS12345678910",
       |				"incomeSourceType": "02",
       |				"submissionTimestamp": "2019-07-13T07:51:43Z",
       |				"lossType": "income",
       |				"taxYearLossIncurred": 2018,
       |				"currentLossValue": 20101,
       |				"mtdLoss": true
       |			},
       |			{
       |				"lossId": "LLIS12345678904",
       |				"incomeSourceId": "AdIS12345678910",
       |				"incomeSourceType": "04",
       |				"submissionTimestamp": "2019-07-13T07:51:43Z",
       |				"lossType": "income",
       |				"taxYearLossIncurred": 2018,
       |				"currentLossValue": 40101,
       |				"mtdLoss": true
       |			}
       |		],
       |		"claims": [{
       |				"claimId": "CCIS12345678901",
       |				"originatingClaimId": "000000000000211",
       |				"incomeSourceId": "AaIS12345678910",
       |				"incomeSourceType": "01",
       |				"submissionTimestamp": "2019-08-13T07:51:43Z",
       |				"taxYearClaimMade": 2018,
       |				"claimType": "CF"
       |			},
       |			{
       |				"claimId": "CCIS12345678902",
       |				"originatingClaimId": "000000000000212",
       |				"incomeSourceId": "AbIS12345678910",
       |				"incomeSourceType": "01",
       |				"submissionTimestamp": "2019-08-13T07:51:43Z",
       |				"taxYearClaimMade": 2018,
       |				"claimType": "CSGI"
       |			},
       |			{
       |				"claimId": "CCIS12345678903",
       |				"originatingClaimId": "000000000000213",
       |				"incomeSourceId": "AcIS12345678910",
       |				"incomeSourceType": "02",
       |				"submissionTimestamp": "2019-08-13T07:51:43Z",
       |				"taxYearClaimMade": 2018,
       |				"claimType": "CSFHL"
       |			},
       |			{
       |				"claimId": "CCIS12345678904",
       |				"originatingClaimId": "000000000000214",
       |				"incomeSourceId": "AdIS12345678910",
       |				"incomeSourceType": "04",
       |				"submissionTimestamp": "2019-08-13T07:51:43Z",
       |				"taxYearClaimMade": 2018,
       |				"claimType": "CFCSGI"
       |			},
       |			{
       |				"claimId": "CCIS12345678921",
       |				"originatingClaimId": "000000000000221",
       |				"incomeSourceId": "AaIS12345678910",
       |				"incomeSourceType": "01",
       |				"submissionTimestamp": "2019-08-13T07:51:43Z",
       |				"taxYearClaimMade": 2018,
       |				"claimType": "CF"
       |			},
       |			{
       |				"claimId": "CCIS12345678922",
       |				"originatingClaimId": "000000000000222",
       |				"incomeSourceId": "AbIS12345678910",
       |				"incomeSourceType": "01",
       |				"submissionTimestamp": "2019-08-13T07:51:43Z",
       |				"taxYearClaimMade": 2018,
       |				"claimType": "CSGI"
       |			},
       |			{
       |				"claimId": "CCIS12345678923",
       |				"originatingClaimId": "000000000000223",
       |				"incomeSourceId": "AcIS12345678910",
       |				"incomeSourceType": "02",
       |				"submissionTimestamp": "2019-08-13T07:51:43Z",
       |				"taxYearClaimMade": 2018,
       |				"claimType": "CSFHL"
       |			},
       |			{
       |				"claimId": "CCIS12345678924",
       |				"originatingClaimId": "000000000000224",
       |				"incomeSourceId": "AdIS12345678910",
       |				"incomeSourceType": "04",
       |				"submissionTimestamp": "2019-08-13T07:51:43Z",
       |				"taxYearClaimMade": 2018,
       |				"claimType": "CFCSGI"
       |			}
       |		]
       |	},
       |	"calculation": {
       |		"allowancesAndDeductions": {
       |			"personalAllowance": 8001,
       |			"reducedPersonalAllowance": 8002,
       |			"giftOfInvestmentsAndPropertyToCharity": 8003,
       |			"blindPersonsAllowance": 8004,
       |			"lossesAppliedToGeneralIncome": 8005,
       |			"qualifyingLoanInterestFromInvestments": 5000.99,
       |			"post-cessationTradeReceipts": 5000.99,
       |			"paymentsToTradeUnionsForDeathBenefits": 5000.99,
       |			"grossAnnuityPayments": 5000.99
       |		},
       |		"reliefs": {
       |			"reliefsClaimed": [{
       |					"type": "vctSubscriptions",
       |					"amountClaimed": 5000.99,
       |					"allowableAmount": 5000.99,
       |					"amountUsed": 5000.99,
       |					"rate": 20
       |				},
       |				{
       |					"type": "deficiencyRelief",
       |					"amountClaimed": 5000.99,
       |					"allowableAmount": 5000.99,
       |					"amountUsed": 5001.99,
       |					"rate": 20
       |				},
       |				{
       |					"type": "eisSubscriptions",
       |					"amountClaimed": 5000.99,
       |					"allowableAmount": 5000.99,
       |					"amountUsed": 5002.99,
       |					"rate": 20
       |				},
       |				{
       |					"type": "seedEnterpriseInvestment",
       |					"amountClaimed": 5000.99,
       |					"allowableAmount": 5000.99,
       |					"amountUsed": 5003.99,
       |					"rate": 20
       |				},
       |				{
       |					"type": "communityInvestment",
       |					"amountClaimed": 5000.99,
       |					"allowableAmount": 5000.99,
       |					"amountUsed": 5004.99,
       |					"rate": 20
       |				},
       |				{
       |					"type": "socialEnterpriseInvestment",
       |					"amountClaimed": 5000.99,
       |					"allowableAmount": 5000.99,
       |					"amountUsed": 5005.99,
       |					"rate": 20
       |				},
       |				{
       |					"type": "maintenancePayments",
       |					"amountClaimed": 5000.99,
       |					"allowableAmount": 5000.99,
       |					"amountUsed": 5006.99,
       |					"rate": 20
       |				},
       |				{
       |					"type": "qualifyingDistributionRedemptionOfSharesAndSecurities",
       |					"amountClaimed": 5000.99,
       |					"allowableAmount": 5000.99,
       |					"amountUsed": 5007.99,
       |					"rate": 20
       |				},
       |				{
       |					"type": "nonDeductableLoanInterest",
       |					"amountClaimed": 5000.99,
       |					"allowableAmount": 5000.99,
       |					"amountUsed": 5008.99,
       |					"rate": 20
       |				}
       |			]
       |		},
       |		"taxDeductedAtSource": {
       |			"bbsi": 8009.99,
       |			"ukLandAndProperty": 8010.99,
       |			"voidedIsa": 8011.99,
       |			"specialWithholdingTaxOrUkTaxPaid": 8012.99
       |		},
       |		"giftAid": {
       |			"grossGiftAidPayments": 8011,
       |			"rate": 35,
       |			"giftAidTax": 8012.11
       |		},
       |		"businessProfitAndLoss": [{
       |				"incomeSourceId": "AaIS12345678910",
       |				"incomeSourceType": "01",
       |				"incomeSourceName": "Self-Employment Business ONE",
       |				"totalIncome": 100101.11,
       |				"totalExpenses": 100201.11,
       |				"netProfit": 100301.11,
       |				"netLoss": 100401.11,
       |				"totalAdditions": 100501.11,
       |				"totalDeductions": 100601.11,
       |				"accountingAdjustments": 100701.11,
       |				"taxableProfit": 100801,
       |				"adjustedIncomeTaxLoss": 100901,
       |				"totalBroughtForwardIncomeTaxLosses": 101001,
       |				"lossForCSFHL": 101101,
       |				"broughtForwardIncomeTaxLossesUsed": 101201,
       |				"carrySidewaysIncomeTaxLossesUsed": 101211,
       |				"taxableProfitAfterIncomeTaxLossesDeduction": 101301,
       |				"totalIncomeTaxLossesCarriedForward": 101401,
       |				"class4Loss": 101501,
       |				"totalBroughtForwardClass4Losses": 101601,
       |				"broughtForwardClass4LossesUsed": 101701,
       |				"carrySidewaysClass4LossesUsed": 101801,
       |				"totalClass4LossesCarriedForward": 101901
       |			},
       |			{
       |				"incomeSourceId": "AbIS12345678910",
       |				"incomeSourceType": "01",
       |				"incomeSourceName": "Self-Employment Business TWO",
       |				"totalIncome": 100102.22,
       |				"totalExpenses": 100202.22,
       |				"netProfit": 100302.22,
       |				"netLoss": 100402.22,
       |				"totalAdditions": 100502.22,
       |				"totalDeductions": 100602.22,
       |				"accountingAdjustments": 100702.22,
       |				"taxableProfit": 100802,
       |				"adjustedIncomeTaxLoss": 100902,
       |				"totalBroughtForwardIncomeTaxLosses": 101002,
       |				"lossForCSFHL": 101102,
       |				"broughtForwardIncomeTaxLossesUsed": 101202,
       |				"carrySidewaysIncomeTaxLossesUsed": 101212,
       |				"taxableProfitAfterIncomeTaxLossesDeduction": 101302,
       |				"totalIncomeTaxLossesCarriedForward": 101402,
       |				"class4Loss": 101502,
       |				"totalBroughtForwardClass4Losses": 101602,
       |				"broughtForwardClass4LossesUsed": 101702,
       |				"carrySidewaysClass4LossesUsed": 101802,
       |				"totalClass4LossesCarriedForward": 101902
       |			},
       |			{
       |				"incomeSourceId": "AcIS12345678910",
       |				"incomeSourceType": "02",
       |				"incomeSourceName": "UK Property Non-FHL",
       |				"totalIncome": 2001.11,
       |				"totalExpenses": 2002.11,
       |				"netProfit": 2003.11,
       |				"netLoss": 2004.11,
       |				"totalAdditions": 2005.11,
       |				"totalDeductions": 2006.11,
       |				"accountingAdjustments": 2007.11,
       |				"taxableProfit": 2008,
       |				"adjustedIncomeTaxLoss": 2009,
       |				"totalBroughtForwardIncomeTaxLosses": 2010,
       |				"lossForCSFHL": 2011,
       |				"broughtForwardIncomeTaxLossesUsed": 2012,
       |				"carrySidewaysIncomeTaxLossesUsed": 20121,
       |				"taxableProfitAfterIncomeTaxLossesDeduction": 2013,
       |				"totalIncomeTaxLossesCarriedForward": 2014,
       |				"class4Loss": 2015,
       |				"totalBroughtForwardClass4Losses": 2016,
       |				"broughtForwardClass4LossesUsed": 2017,
       |				"carrySidewaysClass4LossesUsed": 2018,
       |				"totalClass4LossesCarriedForward": 2019
       |			},
       |			{
       |				"incomeSourceId": "AdIS12345678910",
       |				"incomeSourceType": "04",
       |				"incomeSourceName": "UK Property FHL",
       |				"totalIncome": 4001.11,
       |				"totalExpenses": 4002.11,
       |				"netProfit": 4003.11,
       |				"netLoss": 4004.11,
       |				"totalAdditions": 4005.11,
       |				"totalDeductions": 4006.11,
       |				"accountingAdjustments": 4007.11,
       |				"taxableProfit": 4008,
       |				"adjustedIncomeTaxLoss": 4009,
       |				"totalBroughtForwardIncomeTaxLosses": 4010,
       |				"lossForCSFHL": 4011,
       |				"broughtForwardIncomeTaxLossesUsed": 4012,
       |				"taxableProfitAfterIncomeTaxLossesDeduction": 4013,
       |				"totalIncomeTaxLossesCarriedForward": 4014,
       |				"class4Loss": 4015,
       |				"totalBroughtForwardClass4Losses": 4016,
       |				"broughtForwardClass4LossesUsed": 4017,
       |				"carrySidewaysClass4LossesUsed": 4018,
       |				"totalClass4LossesCarriedForward": 4019
       |			}
       |		],
       |		"savingsAndGainsIncome": {
       |			"totalUkSavingsAndGains": 7000,
       |			"chargeableForeignSavingsAndGains": 7001,
       |			"ukSavingsAndGainsIncome": [{
       |					"incomeSourceId": "SAVKB1UVwUTBQGJ",
       |					"incomeSourceType": "09",
       |					"incomeSourceName": "UK Savings Account ONE",
       |					"grossIncome": 90101.11,
       |					"netIncome": 90201.11,
       |					"taxDeducted": 90301.11
       |				},
       |				{
       |					"incomeSourceId": "SAVKB2UVwUTBQGJ",
       |					"incomeSourceType": "09",
       |					"incomeSourceName": "UK Savings Account TWO",
       |					"grossIncome": 90102.11,
       |					"netIncome": 90202.11,
       |					"taxDeducted": 90302.11
       |				}
       |			]
       |		},
       |		"incomeSummaryTotals": {
       |			"totalSelfEmploymentProfit": 6001,
       |			"totalPropertyProfit": 6002,
       |			"totalFHLPropertyProfit": 6003,
       |			"totalUKOtherPropertyProfit": 6004,
       |			"totalForeignPropertyProfit": 6005,
       |			"totalEeaFhlProfit": 6006
       |		},
       |		"shareSchemesIncome": {
       |			"totalIncome": 6015.99
       |		},
       |		"foreignIncome": {
       |			"chargeableOverseasPensionsStateBenefitsRoyalties": 6016.99,
       |			"chargeableAllOtherIncomeReceivedWhilstAbroad": 6017.99,
       |			"overseasIncomeAndGains": {
       |				"gainAmount": 6018.99
       |			},
       |			"totalForeignBenefitsAndGifts": 6019.99
       |		},
       |		"chargeableEventGainsIncome": {
       |			"totalOfAllGains": 7000
       |		},
       |		"taxCalculation": {
       |			"incomeTax": {
       |				"totalIncomeReceivedFromAllSources": 7001,
       |				"totalAllowancesAndDeductions": 7002,
       |				"totalTaxableIncome": 7003,
       |				"payPensionsProfit": {
       |					"incomeReceived": 7004,
       |					"allowancesAllocated": 7005,
       |					"taxableIncome": 7006,
       |					"incomeTaxAmount": 7007.11,
       |					"taxBands": [{
       |							"name": "SRT",
       |							"rate": 19,
       |							"bandLimit": 2084,
       |							"apportionedBandLimit": 2084,
       |							"income": 2084,
       |							"taxAmount": 395.96
       |						},
       |						{
       |							"name": "BRT",
       |							"rate": 20,
       |							"bandLimit": 10572,
       |							"apportionedBandLimit": 10572,
       |							"income": 10574,
       |							"taxAmount": 2114.40
       |						},
       |						{
       |							"name": "IRT",
       |							"rate": 21,
       |							"bandLimit": 18271,
       |							"apportionedBandLimit": 18271,
       |							"income": 18271,
       |							"taxAmount": 3836.91
       |						},
       |						{
       |							"name": "HRT",
       |							"rate": 41,
       |							"bandLimit": 106569,
       |							"apportionedBandLimit": 106569,
       |							"income": 106569,
       |							"taxAmount": 43693.29
       |						},
       |						{
       |							"name": "ART",
       |							"rate": 46,
       |							"bandLimit": 100000,
       |							"apportionedBandLimit": 100000,
       |							"income": 100000,
       |							"taxAmount": 46000.00
       |						}
       |					]
       |				},
       |				"savingsAndGains": {
       |					"incomeReceived": 7012,
       |					"allowancesAllocated": 7013,
       |					"taxableIncome": 7014,
       |					"incomeTaxAmount": 7015.11,
       |					"taxBands": [{
       |							"name": "SSR",
       |							"rate": 10,
       |							"bandLimit": 1000,
       |							"apportionedBandLimit": 1000,
       |							"income": 1000,
       |							"taxAmount": 100.00
       |						},
       |						{
       |							"name": "BRT",
       |							"rate": 20,
       |							"bandLimit": 5000,
       |							"apportionedBandLimit": 5000,
       |							"income": 5000,
       |							"taxAmount": 1000.00
       |						},
       |						{
       |							"name": "ZRTBR",
       |							"rate": 0,
       |							"bandLimit": 1000,
       |							"apportionedBandLimit": 1000,
       |							"income": 1000,
       |							"taxAmount": 0.00
       |						},
       |						{
       |							"name": "HRT",
       |							"rate": 40,
       |							"bandLimit": 10000,
       |							"apportionedBandLimit": 10000,
       |							"income": 10000,
       |							"taxAmount": 4000.00
       |						},
       |						{
       |							"name": "ZRTHR",
       |							"rate": 0,
       |							"bandLimit": 2000,
       |							"apportionedBandLimit": 1000,
       |							"income": 2000,
       |							"taxAmount": 0.00
       |						},
       |						{
       |							"name": "ART",
       |							"rate": 45,
       |							"bandLimit": 20000,
       |							"apportionedBandLimit": 20000,
       |							"income": 20000,
       |							"taxAmount": 9000.00
       |						}
       |					]
       |				},
       |				"dividends": {
       |					"incomeReceived": 7020,
       |					"allowancesAllocated": 7021,
       |					"taxableIncome": 7022,
       |					"incomeTaxAmount": 7023.11,
       |					"taxBands": [{
       |							"name": "BRT",
       |							"rate": 20,
       |							"bandLimit": 5000,
       |							"apportionedBandLimit": 5000,
       |							"income": 5000,
       |							"taxAmount": 1000.00
       |						},
       |						{
       |							"name": "ZRTBR",
       |							"rate": 0,
       |							"bandLimit": 1000,
       |							"apportionedBandLimit": 1000,
       |							"income": 1000,
       |							"taxAmount": 0.00
       |						},
       |						{
       |							"name": "HRT",
       |							"rate": 40,
       |							"bandLimit": 10000,
       |							"apportionedBandLimit": 10000,
       |							"income": 10000,
       |							"taxAmount": 4000.00
       |						},
       |						{
       |							"name": "ZRTHR",
       |							"rate": 0,
       |							"bandLimit": 2000,
       |							"apportionedBandLimit": 2000,
       |							"income": 2000,
       |							"taxAmount": 0.00
       |						},
       |						{
       |							"name": "ART",
       |							"rate": 45,
       |							"bandLimit": 20000,
       |							"apportionedBandLimit": 20000,
       |							"income": 20000,
       |							"taxAmount": 9000.00
       |						},
       |						{
       |							"name": "ZRTAR",
       |							"rate": 0,
       |							"bandLimit": 4000,
       |							"apportionedBandLimit": 4000,
       |							"income": 4000,
       |							"taxAmount": 0.00
       |						}
       |					]
       |				},
       |				"incomeTaxCharged": 7028,
       |				"totalReliefs": 7029.25,
       |				"incomeTaxDueAfterReliefs": 7030.11,
       |				"incomeTaxDueAfterGiftAid": 7031.11,
       |				"lumpSums": {
       |					"incomeReceived": 8001,
       |					"taxableIncome": 8002,
       |					"allowancesAllocated": 12500,
       |					"incomeTaxAmount": 5000,
       |					"taxBands": [{
       |							"name": "BRT",
       |							"rate": 20,
       |							"bandLimit": 12500,
       |							"apportionedBandLimit": 12500,
       |							"income": 12500,
       |							"taxAmount": 5000
       |						},
       |						{
       |							"name": "SRT",
       |							"rate": 21,
       |							"bandLimit": 12501,
       |							"apportionedBandLimit": 12501,
       |							"income": 12501,
       |							"taxAmount": 5001
       |						},
       |						{
       |							"name": "HRT",
       |							"rate": 22,
       |							"bandLimit": 12502,
       |							"apportionedBandLimit": 12502,
       |							"income": 12502,
       |							"taxAmount": 5002
       |						},
       |						{
       |							"name": "ART",
       |							"rate": 23,
       |							"bandLimit": 12503,
       |							"apportionedBandLimit": 12503,
       |							"income": 12503,
       |							"taxAmount": 5003
       |						}
       |					]
       |				},
       |				"gainsOnLifePolicies": {
       |					"incomeReceived": 8003,
       |					"taxableIncome": 8004,
       |					"allowancesAllocated": 12500,
       |					"incomeTaxAmount": 5000.99,
       |					"taxBands": [{
       |							"name": "ART",
       |							"rate": 20,
       |							"bandLimit": 12500,
       |							"apportionedBandLimit": 12500,
       |							"income": 12500,
       |							"taxAmount": 5000.99
       |						},
       |						{
       |							"name": "SSR",
       |							"rate": 21,
       |							"bandLimit": 12501,
       |							"apportionedBandLimit": 12501,
       |							"income": 12501,
       |							"taxAmount": 5001.99
       |						},
       |						{
       |							"name": "ZRTBR",
       |							"rate": 22,
       |							"bandLimit": 12502,
       |							"apportionedBandLimit": 12502,
       |							"income": 12502,
       |							"taxAmount": 5002.99
       |						},
       |						{
       |							"name": "BRT",
       |							"rate": 23,
       |							"bandLimit": 12503,
       |							"apportionedBandLimit": 12503,
       |							"income": 12503,
       |							"taxAmount": 5003.99
       |						},
       |						{
       |							"name": "ZRTHR",
       |							"rate": 24,
       |							"bandLimit": 12504,
       |							"apportionedBandLimit": 12504,
       |							"income": 12504,
       |							"taxAmount": 5004.99
       |						},
       |						{
       |							"name": "HRT",
       |							"rate": 25,
       |							"bandLimit": 12505,
       |							"apportionedBandLimit": 12505,
       |							"income": 12505,
       |							"taxAmount": 5005.99
       |						},
       |						{
       |							"name": "ZRTAR",
       |							"rate": 26,
       |							"bandLimit": 12506,
       |							"apportionedBandLimit": 12506,
       |							"income": 12506,
       |							"taxAmount": 5006.99
       |						}
       |					]
       |				}
       |			},
       |			"nics": {
       |				"class2Nics": {
       |					"amount": 5001.11,
       |					"weeklyRate": 5002.11,
       |					"weeks": 23,
       |					"limit": 5004,
       |					"apportionedLimit": 5005,
       |					"underSmallProfitThreshold": false,
       |					"actualClass2Nic": false
       |				},
       |				"class4Nics": {
       |					"totalIncomeLiableToClass4Charge": 5006,
       |					"totalClass4LossesAvailable": 5007,
       |					"totalClass4LossesUsed": 5008,
       |					"totalClass4LossesCarriedForward": 5009,
       |					"totalIncomeChargeableToClass4": 5010,
       |					"totalAmount": 5011.11,
       |					"nic4Bands": [{
       |							"name": "ZRT",
       |							"rate": 0,
       |							"threshold": 9500,
       |							"apportionedThreshold": 9500,
       |							"income": 9500,
       |							"amount": 0.00
       |						},
       |						{
       |							"name": "BRT",
       |							"rate": 10.25,
       |							"threshold": 40500,
       |							"apportionedThreshold": 40500,
       |							"income": 40500,
       |							"amount": 4151.25
       |						},
       |						{
       |							"name": "HRT",
       |							"rate": 3.25,
       |							"threshold": 100000,
       |							"apportionedThreshold": 100000,
       |							"income": 5000,
       |							"amount": 162.50
       |						}
       |					]
       |				},
       |				"nic2NetOfDeductions": 5016.11,
       |				"nic4NetOfDeductions": 5017.11,
       |				"totalNic": 5018.11
       |			},
       |			"totalIncomeTaxNicsCharged": 5019.11,
       |			"totalTaxDeducted": 5020,
       |			"totalIncomeTaxAndNicsDue": 5021.11
       |		},
       |		"dividendsIncome": {
       |			"totalUkDividends": 6999,
       |			"chargeableForeignDividends": 6998
       |		},
       |		"previousCalculation": {
       |			"calculationTimestamp": "2019-02-15T09:35:15.094Z",
       |			"calculationId": "12345678",
       |			"totalIncomeTaxAndNicsDue": 5022.11,
       |			"incomeTaxNicDueThisPeriod": 5023.11
       |		},
       |		"endOfYearEstimate": {
       |			"incomeSource": [{
       |					"incomeSourceId": "AaIS12345678910",
       |					"incomeSourceType": "01",
       |					"incomeSourceName": "Self-Employment Business ONE",
       |					"taxableIncome": 10001,
       |					"finalised": true
       |				},
       |				{
       |					"incomeSourceId": "AbIS12345678910",
       |					"incomeSourceType": "01",
       |					"incomeSourceName": "Self-Employment Business TWO",
       |					"taxableIncome": 10002,
       |					"finalised": true
       |				},
       |				{
       |					"incomeSourceId": "AcIS12345678910",
       |					"incomeSourceType": "02",
       |					"incomeSourceName": "UK Property Non FHL",
       |					"taxableIncome": 20001,
       |					"finalised": true
       |				},
       |				{
       |					"incomeSourceId": "AdIS12345678910",
       |					"incomeSourceType": "04",
       |					"incomeSourceName": "UK Property FHL",
       |					"taxableIncome": 40001,
       |					"finalised": true
       |				},
       |				{
       |					"incomeSourceId": "SAVKB1UVwUTBQGJ",
       |					"incomeSourceType": "09",
       |					"incomeSourceName": "UK Savings Account ONE",
       |					"taxableIncome": 90001,
       |					"finalised": true
       |				},
       |				{
       |					"incomeSourceId": "SAVKB2UVwUTBQGJ",
       |					"incomeSourceType": "09",
       |					"incomeSourceName": "UK Savings Account TWO",
       |					"taxableIncome": 90002,
       |					"finalised": true
       |				},
       |				{
       |					"incomeSourceId": "DDIS12345678910",
       |					"incomeSourceType": "10",
       |					"incomeSourceName": "UK Dividends",
       |					"taxableIncome": 10001,
       |					"finalised": true
       |				}
       |			],
       |			"totalEstimatedIncome": 6005,
       |			"totalTaxableIncome": 6006,
       |			"incomeTaxAmount": 6007.11,
       |			"nic2": 6008.11,
       |			"nic4": 6009.11,
       |			"totalNicAmount": 6010.11,
       |			"incomeTaxNicAmount": 6011.11
       |		},
       |		"lossesAndClaims": {
       |			"resultOfClaimsApplied": [{
       |					"claimId": "CCIS12345678901",
       |					"originatingClaimId": "000000000000211",
       |					"incomeSourceId": "AaIS12345678910",
       |					"incomeSourceType": "01",
       |					"taxYearClaimMade": 2018,
       |					"claimType": "CF",
       |					"mtdLoss": true,
       |					"taxYearLossIncurred": 2018,
       |					"lossAmountUsed": 10101,
       |					"remainingLossValue": 10201,
       |					"lossType": "income"
       |				},
       |				{
       |					"claimId": "CCIS12345678902",
       |					"originatingClaimId": "000000000000212",
       |					"incomeSourceId": "AbIS12345678910",
       |					"incomeSourceType": "01",
       |					"taxYearClaimMade": 2018,
       |					"claimType": "CSGI",
       |					"mtdLoss": true,
       |					"taxYearLossIncurred": 2018,
       |					"lossAmountUsed": 10102,
       |					"remainingLossValue": 10202,
       |					"lossType": "income"
       |				},
       |				{
       |					"claimId": "CCIS12345678903",
       |					"originatingClaimId": "000000000000213",
       |					"incomeSourceId": "AcIS12345678910",
       |					"incomeSourceType": "02",
       |					"taxYearClaimMade": 2018,
       |					"claimType": "CSFHL",
       |					"mtdLoss": true,
       |					"taxYearLossIncurred": 2018,
       |					"lossAmountUsed": 20101,
       |					"remainingLossValue": 20201,
       |					"lossType": "income"
       |				},
       |				{
       |					"claimId": "CCIS12345678904",
       |					"originatingClaimId": "000000000000214",
       |					"incomeSourceId": "AdIS12345678910",
       |					"incomeSourceType": "04",
       |					"taxYearClaimMade": 2018,
       |					"claimType": "CFCSGI",
       |					"mtdLoss": true,
       |					"taxYearLossIncurred": 2018,
       |					"lossAmountUsed": 40101,
       |					"remainingLossValue": 40201,
       |					"lossType": "income"
       |				}
       |			],
       |			"unclaimedLosses": [{
       |					"incomeSourceId": "AaIS12345678910",
       |					"incomeSourceType": "01",
       |					"taxYearLossIncurred": 2018,
       |					"currentLossValue": 1001,
       |					"lossType": "income"
       |				},
       |				{
       |					"incomeSourceId": "AbIS12345678910",
       |					"incomeSourceType": "01",
       |					"taxYearLossIncurred": 2018,
       |					"currentLossValue": 1002,
       |					"lossType": "income"
       |				}
       |			],
       |			"carriedForwardLosses": [{
       |					"claimId": "CCIS12345678901",
       |					"originatingClaimId": "000000000000211",
       |					"incomeSourceId": "AaIS12345678910",
       |					"incomeSourceType": "01",
       |					"claimType": "CF",
       |					"taxYearClaimMade": 2019,
       |					"taxYearLossIncurred": 2018,
       |					"currentLossValue": 1001,
       |					"lossType": "income"
       |				},
       |				{
       |					"claimId": "CCIS12345678902",
       |					"originatingClaimId": "000000000000212",
       |					"incomeSourceId": "AbIS12345678910",
       |					"incomeSourceType": "01",
       |					"claimType": "CF",
       |					"taxYearClaimMade": 2019,
       |					"taxYearLossIncurred": 2018,
       |					"currentLossValue": 1002,
       |					"lossType": "income"
       |				}
       |			],
       |			"defaultCarriedForwardLosses": [{
       |					"incomeSourceId": "AcIS12345678910",
       |					"incomeSourceType": "02",
       |					"taxYearLossIncurred": 2018,
       |					"currentLossValue": 201
       |				},
       |				{
       |					"incomeSourceId": "AdIS12345678910",
       |					"incomeSourceType": "04",
       |					"taxYearLossIncurred": 2018,
       |					"currentLossValue": 401
       |				}
       |			],
       |			"claimsNotApplied": [{
       |					"claimId": "CCIS12345678921",
       |					"incomeSourceId": "AaIS12345678910",
       |					"incomeSourceType": "01",
       |					"taxYearClaimMade": 2018,
       |					"claimType": "CF"
       |				},
       |				{
       |					"claimId": "CCIS12345678922",
       |					"incomeSourceId": "AbIS12345678910",
       |					"incomeSourceType": "01",
       |					"taxYearClaimMade": 2018,
       |					"claimType": "CSGI"
       |				},
       |				{
       |					"claimId": "CCIS12345678925",
       |					"incomeSourceId": "AcIS12345678910",
       |					"incomeSourceType": "02",
       |					"taxYearClaimMade": 2018,
       |					"claimType": "CF"
       |				}
       |			]
       |		}
       |	},
       |	"messages": {
       |		"info": [{
       |			"id": "C11101",
       |			"text": "You have entered a large amount in total Gift Aid payments. Please check"
       |		}],
       |		"warnings": [{
       |			"id": "C11102",
       |			"text": "Total amount of one-off Gift Aid payments cannot exceed the total gift aid payments. Please check."
       |		}]
       |	}
       |}""".stripMargin
  }
}
