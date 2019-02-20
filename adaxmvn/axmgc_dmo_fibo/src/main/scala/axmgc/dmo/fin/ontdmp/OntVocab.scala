package axmgc.dmo.fin.ontdmp

trait OntVocab // Scala source file marker

trait StdGenVocab {
	val baseUriTxt_owl = "http://www.w3.org/2002/07/owl#"
	val baseUriTxt_rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	val baseUriTxt_rdfs = "http://www.w3.org/2000/01/rdf-schema#"
	val baseUriTxt_skos = "http://www.w3.org/2004/02/skos/core#"
	val baseUriTxt_xml = "http://www.w3.org/XML/1998/namespace"
	val baseUriTxt_xsd = "http://www.w3.org/2001/XMLSchema#"

	val propLN_owlImports = "imports"
	val propLN_rdfType = "type"
}
trait FiboVocab extends StdGenVocab {
	val path_fiboOnt = "gdat/fibo_ont/fibo_2018Q4_all_4MB.ttl"

	val baseUriTxt_omgSm = "http://www.omg.org/techprocess/ab/SpecificationMetadata/"

}

/*
http://www.omg.org/techprocess/ab/SpecificationMetadata/Module,
http://www.w3.org/2000/01/rdf-schema#Datatype,
http://www.w3.org/2002/07/owl#AllDifferent,
http://www.w3.org/2002/07/owl#AnnotationProperty,
http://www.w3.org/2002/07/owl#Class,
http://www.w3.org/2002/07/owl#DatatypeProperty,
http://www.w3.org/2002/07/owl#FunctionalProperty,
http://www.w3.org/2002/07/owl#InverseFunctionalProperty,
http://www.w3.org/2002/07/owl#NamedIndividual,
http://www.w3.org/2002/07/owl#ObjectProperty,
http://www.w3.org/2002/07/owl#Ontology,
http://www.w3.org/2002/07/owl#Restriction,
http://www.w3.org/2002/07/owl#SymmetricProperty,
http://www.w3.org/2002/07/owl#TransitiveProperty,
https://spec.edmcouncil.org/fibo/ontology/BE/Corporations/Corporations/PrivatelyHeldCompany,
https://spec.edmcouncil.org/fibo/ontology/BE/Corporations/Corporations/RegistrationIdentifier
...
AccountingFramework, AgentForServiceOfProcess, BoardOfDirectors, BusinessCenter,
BusinessCenterCode, BusinessCenterCodeScheme, BusinessDayAdjustment, BusinessDayAdjustmentCode, BusinessDayConvention,
BusinessRecurrenceIntervalConvention, BusinessRegistrationAuthority, BusinessRegistry, CalendarPeriod, CentralBank,
CentralCounterpartyClearingHouse, CentralSecuritiesDepository, ClearingBank, CodeSet, Corporation, Currency,
CurrencyIdentifier, Date, DeJureControllingInterestParty, DesignatedContractMarket,
DividendDistributionMethod, EntityControllingParty, EntityLegalFormScheme, EntityStatus,
ExchangeSpecificSecuritiesRegistry, ExchangeSpecificSecurityIdentificationScheme, ExplicitDuration,
ExplicitRecurrenceInterval, FDICCertificateNumber, FRSStateMemberBank, FederalGovernment, FederalReserveDistrict,
FederalReserveDistrictBank, FederalReserveDistrictIdentifier, FederatedSovereignty, FinanceCompany,
FinancialHoldingCompany, FinancialInformationPublisher, FinancialInstrumentIdentificationScheme,
FinancialInstrumentIdentifier, FinancialServiceProvider, FormalOrganization, Funds,
FundsIdentifier, Government, GovernmentAgency, GovernmentDepartment, GovernmentIssuedLicense, HoldingCompany,
IdentificationScheme, Instrumentality, InterestRateAuthority, Jurisdiction,
LegalEntityIdentifier, LegalEntityIdentifierRegistry, LegalEntityIdentifierRegistryEntry,
LegalEntityIdentifierScheme, LicenseIdentifier, ListedSecurityIdentifier, LocalOperatingUnit, ManagementCompany,
MarketIdentifierCodeRegistryEntry, MarketIdentifierCodeStatus, MarketSegmentLevelMarket,
MarketSegmentLevelMarketIdentifier, MaturityLevel, Module, Municipality, NationalBank,
NationalGovernment, NationalNumberingAgency, NationalSecuritiesIdentifyingNumber,
NationalSecuritiesIdentifyingNumberRegistry, NationalSecurityIdentificationScheme, NonGovernmentalOrganization,
NotForProfitOrganization,  OperatingLevelMarket, OperatingLevelMarketIdentifier, Partnership,
PreciousMetal, PreciousMetalIdentifier, PrimaryFederalRegulator, PrivateCompanyWithLimitedLiability,
PrivatelyHeldCompany, ProprietarySecurityIdentificationScheme, ProprietarySecurityIdentifier, RegionalGovernment,
RegionalSovereignty, RegisteredAddress, RegistrationAddress, RegistrationAuthority, RegistrationAuthorityCode,
RegistrationIdentifier, RegistrationScheme, RegistrationService, RegistrationStatus, Registry, RegistryIdentifier,
RegulatoryAgency, RelationshipPeriodQualifier, RelationshipStatus, RelativePrice,
ResearchStatisticsSupervisionDiscountIdentifier, RoutingTransitNumber, SelfRegulatingOrganization,
SovereignState, StateCharteredBank, StatisticalInformationPublisher, StockCorporation, SupranationalEntity,
SwapDataRepository, ThirdPartyAgent, TimeDirection, TimeInstant, TimeInterval, TradeLifecycleStage,
Trust, UnitOfAccount, UnitOfAccountIdentifier
 */