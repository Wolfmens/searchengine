package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@Table(name = "page", indexes = {
        @Index(name = "path_index", columnList = "path")})
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    private int id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site siteId;


    @NotNull
    @Column(columnDefinition = "VARCHAR(255)", unique = true)
    private String path;

    @NotNull
    private int code;

    @NotNull
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;
}
