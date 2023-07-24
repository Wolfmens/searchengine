package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity
@Table(name = "`index`")
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    private int id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY,optional = false, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "page_id", nullable = false)
    private Page pageId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "lemma_id", nullable = false)
    private Lemma lemmaId;

    @NotNull
    @Column(name = "`rank`")
    private float rank;

}
