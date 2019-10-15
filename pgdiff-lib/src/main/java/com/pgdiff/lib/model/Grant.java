package com.pgdiff.lib.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Log4j2
@NoArgsConstructor
public class Grant extends CompareInterface<Grant> {

    private String compareName;
    private String schemaName;
    private String type;
    private String relationshipName;
    private String relationshipAcl;
    private String grants;
    private String role;


    public boolean compare(Grant grant) {
        return (this.getCompareName().equals(grant.getCompareName()) && this.getRelationshipAcl().equals(grant.getRelationshipAcl()));
    }

    public String getCreate() { return null; }

    public Alter getAdd(String destinationSchema) {
        return this.getGrants() != null ?
                new Alter(AlterType.ADD_GRANT,
                        String.format("GRANT %s ON %s.%s TO %s;",
                                this.getGrants(), destinationSchema, this.getRelationshipName(), this.getRole())) :
                null;
    }

    public Alter getDrop(String destinationSchema) {
        return this.getGrants() != null ?
                new Alter(AlterType.DROP_GRANT,
                        String.format("REVOKE %s ON %s.%s FROM %s;",
                                this.getGrants(), destinationSchema, this.getRelationshipName(), this.getRole())) :
                null;
    }

    public List<Alter> getChange(Grant grant) { return new ArrayList<>(); }
}
