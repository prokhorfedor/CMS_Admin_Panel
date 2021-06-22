package com.kpi.web.systems.lab3.entity;

import com.kpi.web.systems.lab3.entity.enums.Language;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;
import com.kpi.web.systems.lab3.entity.enums.OrderType;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "PAGE")
public class Page implements Serializable {

    @Id
    @GenericGenerator(name = "gen", strategy = "increment")
    @GeneratedValue(generator = "gen")
    private Long id;

    @NaturalId
    @Column(name = "CODE", unique = true, nullable = false)
    private String code;
    /*
     * title, h2
     */
    @Column(name = "TITLE_UA", nullable = false)
    private String titleUa;

    @Column(name = "TITLE_EN", nullable = false)
    private String titleEn;
    /*
     * short description
     */
    @Column(name = "DESCRIPTION_UA", nullable = false)
    private String descriptionUa;

    @Column(name = "DESCRIPTION_EN", nullable = false)
    private String descriptionEn;

    @Lob
    @Column(name = "CONTENT_UA", nullable = false)
    private String contentUa;

    @Lob
    @Column(name = "CONTENT_EN", nullable = false)
    private String contentEn;

    @Column(name = "IMAGE_URL", nullable = false)
    private String imageUrl;

    @Column(name = "CREATION_DATE", nullable = false)
    private Date creationDate;

    @Column(name = "UPDATE_DATE", nullable = false)
    private Date updateDate;

    @ManyToOne
    @JoinColumn(name = "PARENT_CODE", referencedColumnName = "CODE")
    private Page parentPage;

    @OneToMany(mappedBy = "parentPage", cascade = CascadeType.ALL)
    private List<Page> childPages = new ArrayList<>();
    /*
     * to determine current page position in parent container
     */
    @Column(name = "ORDER_NUM")
    private Integer orderNum;
    /*
     * to determine order type of children in current container
     */
    @Column(name = "ORDER_TYPE")
    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @ManyToOne
    @JoinColumn(name = "ALIAS_PAGE", referencedColumnName = "CODE")
    private Page aliasPage;

    public String getCode(Language language) {
        return Language.UA.equals(language)
                ? "/ua/" + code
                : "/en/" + code;
    }

    public String getTitle(Language language) {
        return Language.UA.equals(language)
                ? titleUa
                : titleEn;
    }

    public String getDesctiption(Language language) {
        return Language.UA.equals(language)
                ? descriptionUa
                : descriptionEn;
    }

    public String getContent(Language language) {
        return Language.UA.equals(language)
                ? contentUa
                : contentEn;
    }

    public Comparator<Page> getOrderTypeOrDefault() {
        return this.orderType != null ? this.orderType.getComparator() : OrderType.DEFAULT.getComparator();
    }
}
