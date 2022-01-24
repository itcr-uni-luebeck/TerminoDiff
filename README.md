# TerminoDiff

TerminoDiff is a graphical application to quickly compare [HL7 FHIR CodeSystem resources](https://www.hl7.org/fhir/codesystem.html).

## Why this app?

Determining how HL7 FHIR CodeSystem resources differ proves to be a very difficult task without specialized tooling, since there are many aspects to consider in these resources. This makes maintenance of well-formed FHIR CodeSystem resources much more difficult than it should be.

| Level | Aspect | Example(-s) | TerminoDiff's Approach |
|---:|---|---|---|
| 0 | Serialization format | JSON vs. XML | Reading using HAPI FHIR allows ignoring wire format,<br>since the formats are semantically identical |
| 1 | Metadata level |  | Presentation as a table (lower half) in the GUI |
| 1.1 | Simple differences | `title`, `name`, `version` | String comparisons |
| 1.2 | Differences within lists | `language`, `version` | (keyed) difference lists, e.g. by using `language.code`<br>as the key |
| 2 | Concept level |  | Presentation as a table (upper half) in the GUI |
| 2.1 | Simple differences | `display`, `designation` | String comparisons |
| 2.2 | Differences within lists | `property`, `designation` | (keyed) difference lists, e.g. by using `property.code`<br>as the key |
| 2.3 | Unilaterality of concepts | Deletions and additions of codes /<br>concepts across versions | Surfacing within the table with dedicated filter and highlight |
| 3 | Edge differences | Deletions and additions of <br>`parent`/`child` properties;<br>other properties linking concepts<br>also considered | Creation and display of a difference graph with multiple<br>color-coded types of edges. |

