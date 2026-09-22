# Greenhouse Board Configuration

Greenhouse is acquired through its public board API, separately from the global `JobOfferProvider` search flow.
No Greenhouse credentials are required for public job retrieval.

Add one enabled board per configured company in `application.properties` or an external Spring configuration file:

```properties
greenhouse.boards[0].board-token=example-company
greenhouse.boards[0].company-name=Example Company
greenhouse.boards[0].enabled=true
```

The board token is the token used in the company's public Greenhouse board URL. Add further boards with indexes
`[1]`, `[2]`, and so on. With no entries, the application starts normally and the Greenhouse section reports that
no boards are configured.

The on-demand Greenhouse screen fetches every enabled board. A failed board is reported without discarding offers
returned by other boards. Board jobs remain transient `JobOffer` values and are evaluated by the existing
Eligibility and Matching engines before explicit selection creates an Opportunity.
