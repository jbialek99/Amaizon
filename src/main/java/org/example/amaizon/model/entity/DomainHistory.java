/*
package org.example.amaizon.model;

import jakarta.persistence.*;
import org.example.amaizon.model.enums.DomainHistoryAction;


@Entity
@Table(name = "domain_histories")
public class DomainHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 10)
    private String ccTld;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DomainHistoryAction action;

}



//



/*



 */
