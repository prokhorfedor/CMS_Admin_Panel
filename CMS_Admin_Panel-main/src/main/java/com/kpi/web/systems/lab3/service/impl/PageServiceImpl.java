package com.kpi.web.systems.lab3.service.impl;

import com.kpi.web.systems.lab3.entity.Page;
import com.kpi.web.systems.lab3.entity.enums.Language;
import com.kpi.web.systems.lab3.entity.enums.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import com.kpi.web.systems.lab3.repository.PageRepository;
import com.kpi.web.systems.lab3.service.PageService;
import com.kpi.web.systems.lab3.statics.StaticFieldsUtils;
import org.springframework.ui.Model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Log4j2
@Service
public class PageServiceImpl implements PageService {

    private final PageRepository pageRepository;

    @Override
    public String purifyPageCode(String pageCode) {
        Page page = findPageByCode(pageCode);
        return page.getAliasPage() != null
                ? page.getAliasPage().getCode()
                : page.getCode();
    }

    @Override
    public Model render(Language language, String pageCode, Model model) {
        Page page = findPageByCode(pageCode);

        model.addAttribute("language", language);
        model.addAttribute("description", buildDescription(page, language));
        model.addAttribute("header", buildHeader(language));
        model.addAttribute("backButton", buildBackButton(page, language));
        model.addAttribute("title", page.getTitle(language));
        model.addAttribute("imageUrl", page.getImageUrl());
        model.addAttribute("content", buildContent(page, language));
        model.addAttribute("footer", buildFooter(language));

        return model;
    }

    @Override
    public List<Page> findChildPages(String parentCode) {
        return pageRepository.findByParentPageCode(parentCode);
    }

    @Override
    public void save(Page page) {
        page.setCreationDate(new Date(System.currentTimeMillis()));
        page.setUpdateDate(new Date(System.currentTimeMillis()));
        updateAlias(page);
        pageRepository.save(page);
    }

    @Override
    public void update(Page page) {
        updateAlias(page);
        Page pageToSave = updateOriginalPage(page);
        pageRepository.save(pageToSave);
    }

    @Override
    public Page delete(String pageCode) {
        Page pageByCode = findPageByCode(pageCode);
        pageRepository.delete(pageByCode);
        return pageByCode;
    }

    @Override
    public void getIndexPage(String parentCode, Language language, MessageType messageType, Model model) {
        model.addAttribute("header", buildHeader(language));
        model.addAttribute("content", buildIndexPageContent(parentCode, language, messageType));
        model.addAttribute("footer", buildFooter(language));
    }

    @Override
    public void getCreatePage(Language language, String parentCode, Model model) {
        model.addAttribute("parentCode", parentCode);
        model.addAttribute("header", buildHeader(language));
        model.addAttribute("footer", buildFooter(language));
    }

    @Override
    public void getEditPage(Language language, String pageCode, Model model) {
        model.addAttribute("page", findPageByCode(pageCode));
        model.addAttribute("header", buildHeader(language));
        model.addAttribute("footer", buildFooter(language));
    }

    @Override
    public Page findPageByCode(String pageCode) {
        return pageRepository.findByCode(pageCode).orElse(new Page());
    }


    private String buildDescription(Page page, Language language) {

        return String.format(
                "        <title>%s</title>" +
                        "<meta name=\"description\" content=\"%s\">",
                page.getTitle(language), page.getDesctiption(language)
        );
    }

    private String buildBackButton(Page page, Language language) {
        if (page.getParentPage() == null) {
            return "";
        }
        String parentCode = page.getParentPage().getCode(language);

        return String.format(
                "<a class=\"custom-a back-link\" href=\"%s\">← %s</a>",
                parentCode.equals("/ua/root")
                        ? "/ua"
                        : parentCode.equals("/en/root")
                        ? "/en"
                        : parentCode,
                page.getParentPage().getTitle(language)
        );
    }

    private String buildContent(Page page, Language language) {
        StringBuilder builder = new StringBuilder();

        String baseContent = page.getContent(language);
        builder.append("<h4>")
                .append(baseContent)
                .append("</h4>");

        String childrenContainer = buildChildrenList(page, language);
        builder.append(childrenContainer);

        return builder.toString();
    }

    private String buildChildrenList(Page page, Language language) {
        StringBuilder builder = new StringBuilder();
        builder.append("<div class=\"child-container child-container-list\">");

        List<Page> childPages = new ArrayList<>(page.getChildPages());
        childPages.sort(page.getOrderTypeOrDefault());

        for (Page childPage : childPages) {
            builder.append("<div class=\"child-reference child-reference-list\">")
                    .append(String.format("<img class=\"img-small\" src=\"%s\" />", childPage.getImageUrl()))
                    .append(String.format("<h3>%s</h3>", childPage.getTitle(language)))
                    .append(String.format("<p>%s</p>", childPage.getDesctiption(language)))
                    .append(String.format("<a class=\"custom-a\" href=\"%s\">%s</a>", childPage.getCode(language), StaticFieldsUtils.getOpenTextShortened(language)))
                    .append("</div>");
        }
        builder.append("</div>");

        return builder.toString();
    }

    private String buildHeader(Language language) {
        return String.format(
                "<h2>%s</h2>",
                StaticFieldsUtils.getHeaderText(language)
        );
    }

    private String buildFooter(Language language) {
        return String.format(
                "          <h3>%s</h3>"
                        + "<h5>%s</h5>",
                StaticFieldsUtils.getFooterSignText(language),
                StaticFieldsUtils.getFooterCopyrightsText(language)
        );
    }



    private void updateAlias(Page page) {
        page.setParentPage(findPageByCode(page.getParentPage().getCode()));
        if (!page.getAliasPage().getCode().isEmpty()) {
            page.setAliasPage(findPageByCode(page.getAliasPage().getCode()));
        } else {
            page.setAliasPage(null);
        }
    }

    private Page updateOriginalPage(Page page) {
        Page toUpdate = findPageByCode(page.getCode());
        toUpdate.setDescriptionEn(page.getDescriptionEn());
        toUpdate.setDescriptionUa(page.getDescriptionUa());
        toUpdate.setCode(page.getCode());
        toUpdate.setContentEn(page.getContentEn());
        toUpdate.setContentUa(page.getContentUa());
        toUpdate.setImageUrl(page.getImageUrl());
        toUpdate.setTitleEn(page.getTitleEn());
        toUpdate.setTitleUa(page.getTitleUa());
        toUpdate.setOrderNum(page.getOrderNum());
        toUpdate.setOrderType(page.getOrderType());
        toUpdate.setAliasPage(page.getAliasPage());
        toUpdate.setParentPage(page.getParentPage());
        toUpdate.setUpdateDate(new Date(System.currentTimeMillis()));
        return toUpdate;
    }



    private String buildIndexPageContent(String parentCode, Language language, MessageType messageType) {
        return new StringBuilder().append(buildMessageBlock(language, messageType))
                .append(buildAdminBackButton(parentCode, language))
                .append(buildIndexPageHeader(parentCode, language))
                .append(buildIndexTable(parentCode, language))
                .append(buildCreateButton(parentCode, language)).toString();
    }

    private String buildMessageBlock(Language language, MessageType messageType) {
        if (messageType == null) {
            return "";
        }
        switch (messageType) {
            case SAVED:
                return String.format(
                        "<div class=\"alert alert-success mb-4\">%s</div>",
                        StaticFieldsUtils.getSuccessfulPageCreationText(language)
                );
            case UPDATED:
                return String.format(
                        "<div class=\"alert alert-success mb-4\">%s</div>",
                        StaticFieldsUtils.getSuccessfulPageUpdateText(language)
                );
            case DELETED:
                return String.format(
                        "<div class=\"alert alert-danger mb-4\">%s</div>",
                        StaticFieldsUtils.getSuccessfulPageDeletionText(language)
                );
            default:
                return "";
        }
    }

    private String buildAdminBackButton(String parentCode, Language language) {
        Page parentPage = findPageByCode(parentCode).getParentPage();
        return parentCode != null && !parentCode.isEmpty()
                ? String.format("<div class=\"row d-flex justify-content-start mt-4 mb-5 ml-1\">" +
                        "            <a href=\"%s/admin/pages?parentCode=%s\">" +
                        "               <button class=\"btn btn-primary\">← /%s</button>" +
                        "            </a>" +
                        "        </div>",
                Language.UA.equals(language) ? "/ua" : "/en",
                parentPage != null ? parentPage.getCode() : "",
                parentPage != null ? parentPage.getCode() : ""
        )
                : "";
    }

    private String buildIndexPageHeader(String parentCode, Language language) {
        String template = "<h2 class=\"mb-4\">%s</h2>";
        String text = parentCode != null && !parentCode.isEmpty()
                ? String.format(StaticFieldsUtils.getAdminPanelPageHeaderTemplate(language), parentCode)
                : StaticFieldsUtils.getAdminPanelRootPageHeader(language);

        return String.format(template, text);
    }

    private String buildIndexTable(String parentCode, Language language) {
        StringBuilder builder = new StringBuilder();
        builder.append("<table class=\"table-striped table-responsive-lg table-bordered table-sm table-hover shadow rounded mt-5\">");

        String header = buildIndexTableHeader(language);
        builder.append(header);

        String body = buildIndexTableBody(parentCode, language);
        builder.append(body);

        builder.append("</table>");

        return builder.toString();
    }

    private String buildCreateButton(String parentCode, Language language) {
        return parentCode != null && !parentCode.isEmpty()
                ? String.format("<div class=\"row d-flex justify-content-center mt-4\">" +
                        "            <a href=\"%s/admin/pages/create?parentCode=%s\">" +
                        "               <button class=\"btn btn-success\">%s</button>" +
                        "            </a>" +
                        "        </div>",
                Language.UA.equals(language) ? "/ua" : "/en",
                parentCode,
                StaticFieldsUtils.getCreateButtonText(language)
        )
                : "";
    }

    private String buildIndexTableHeader(Language language) {
        StringBuilder builder = new StringBuilder();

        builder.append("<thead>");

        builder.append(buildTableHeaderCell(""));
        builder.append(buildTableHeaderCell("id"));
        builder.append(buildTableHeaderCell(StaticFieldsUtils.getTitleEnText(language)));
        builder.append(buildTableHeaderCell(StaticFieldsUtils.getTitleUaText(language)));
        builder.append(buildTableHeaderCell(StaticFieldsUtils.getCodeText(language)));
        builder.append(buildTableHeaderCell(StaticFieldsUtils.getContentEnText(language)));
        builder.append(buildTableHeaderCell(StaticFieldsUtils.getContentUaText(language)));
        builder.append(buildTableHeaderCell(StaticFieldsUtils.getCreationDateText(language)));
        builder.append(buildTableHeaderCell(StaticFieldsUtils.getImageUrlText(language)));
        builder.append(buildTableHeaderCell(StaticFieldsUtils.getDescriptionEnText(language)));
        builder.append(buildTableHeaderCell(StaticFieldsUtils.getDescriptionUaText(language)));
        builder.append(buildTableHeaderCell(StaticFieldsUtils.getOrderNumText(language)));
        builder.append(buildTableHeaderCell(StaticFieldsUtils.getOrderTypeText(language)));
        builder.append(buildTableHeaderCell(StaticFieldsUtils.getUpdateDateText(language)));
        builder.append(buildTableHeaderCell(StaticFieldsUtils.getAliasPageText(language)));
        builder.append(buildTableHeaderCell(StaticFieldsUtils.getParentCodeText(language)));
        builder.append(buildTableHeaderCell(""));
        builder.append(buildTableHeaderCell(""));
        builder.append(buildTableHeaderCell(""));

        builder.append("</thead>");

        return builder.toString();
    }

    private String buildIndexTableBody(String parentCode, Language language) {
        StringBuilder builder = new StringBuilder();

        builder.append("<tbody>");

        Page parentPage = findPageByCode(parentCode);
        if (parentPage.getCode() != null) {
            for (Page page : parentPage.getChildPages()) {
                String childRow = getChildPageRow(page, language);
                builder.append(childRow);
            }
        } else {
            Page rootPage = findPageByCode("root");
            String rootChildRow = getChildPageRow(rootPage, language);
            builder.append(rootChildRow);
        }
        builder.append("</tbody>");

        return builder.toString();
    }

    private String getChildPageRow(Page page, Language language) {
        return "<tr>" +
                buildTableCell(
                        page.getChildPages().isEmpty() ? "" : buildChildrenButton(page.getCode(), language)) +
                buildTableCell(page.getId().toString()) +
                buildTableCell(ellipsize(emptyIfNull(page.getTitleEn()), 9)) +
                buildTableCell(ellipsize(page.getTitleUa(), 9)) +
                buildTableCell(page.getCode()) +
                buildTableCell(ellipsize(page.getContentEn(), 10)) +
                buildTableCell(ellipsize(page.getContentUa(), 10)) +
                buildTableCell(page.getCreationDate().toString()) +
                buildTableCell(ellipsize(page.getImageUrl(), 9)) +
                buildTableCell(ellipsize(page.getDescriptionEn(), 9)) +
                buildTableCell(ellipsize(page.getDescriptionUa(), 9)) +
                buildTableCell(emptyIfNull(page.getOrderNum())) +
                buildTableCell(emptyIfNull(page.getOrderType())) +
                buildTableCell(page.getUpdateDate().toString()) +
                buildTableCell(page.getAliasPage() != null ? emptyIfNull(page.getAliasPage().getCode()) : "") +
                buildTableCell(page.getParentPage() != null ? emptyIfNull(page.getParentPage().getCode()) : "") +
                buildTableCell(buildShowButton(page.getCode(), language)) +
                buildTableCell(buildUpdateButton(page.getCode(), language)) +
                buildTableCell(buildDeleteButton(page.getCode(), language)) +
                "</tr>";
    }

    private String buildChildrenButton(String code, Language language) {
        return String.format("<a href=\"%s/admin/pages?parentCode=%s\">" +
                        "         <button class=\"btn btn-success\">⟱</button>" +
                        "     </a>",
                language == Language.UA ? "/ua" : "/en",
                code);
    }

    private String buildShowButton(String code, Language language) {
        return String.format("<a href=\"%s/admin/pages/%s\">" +
                        "         <button class=\"btn btn-warning\">%s</button>" +
                        "     </a>",
                language == Language.UA ? "/ua" : "/en",
                code,
                StaticFieldsUtils.getShowButtonText(language));
    }

    private String buildUpdateButton(String code, Language language) {
        return String.format("<a href=\"%s/admin/pages/%s/edit\">" +
                        "         <button class=\"btn btn-warning\">%s</button>" +
                        "     </a>",
                language == Language.UA ? "/ua" : "/en",
                code,
                StaticFieldsUtils.getUpdateButtonText(language));
    }

    private String buildDeleteButton(String code, Language language) {
        return String.format("<form action=\"%s/admin/pages/%s\" method=\"post\">" +
                        "         <input name=\"_method\" type=\"hidden\" value=\"delete\" />" +
                        "         <button class=\"btn btn-warning\">%s</button>" +
                        "     </form>",
                language == Language.UA ? "/ua" : "/en",
                code,
                StaticFieldsUtils.getDeleteButtonText(language));
    }

    private String buildTableCell(String text) {
        return String.format("<td class=\"text-center\"><span>%s</span></td>", text);
    }

    private String buildTableHeaderCell(String text) {
        return String.format("<th class=\"text-center\">%s</th>", text);
    }

    private String emptyIfNull(Object object) {
        return object != null
                ? object.toString()
                : "";
    }

    private String ellipsize(String text, int maxWidth) {
        if (text.length() <= maxWidth) {
            return text;
        }
        return text.substring(0, maxWidth - 3).concat("...");
    }
}
