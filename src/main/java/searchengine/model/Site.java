package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "site")
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    private int id;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Type status;

    @Column(name = "status_time")
    @NotNull
    private LocalDateTime statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(columnDefinition = "VARCHAR(255)")
    @NotNull
    private String url;

    @Column(columnDefinition = "VARCHAR(255)")
    @NotNull
    private String name;

}
