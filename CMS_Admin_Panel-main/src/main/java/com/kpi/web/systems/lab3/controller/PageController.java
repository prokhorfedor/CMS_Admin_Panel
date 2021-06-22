package com.kpi.web.systems.lab3.controller;

import com.kpi.web.systems.lab3.entity.Page;
import com.kpi.web.systems.lab3.entity.enums.Language;
import com.kpi.web.systems.lab3.entity.enums.MessageType;
import com.kpi.web.systems.lab3.service.PageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RequiredArgsConstructor
@Log4j2
@Controller
public class PageController {

    private static final String ADMIN_PREFIX = "/admin";
    private static final String LANGUAGE_PREFIX = "/{languageCode}";

    private final PageService pageService;

    @GetMapping(value = LANGUAGE_PREFIX + ADMIN_PREFIX + "/pages")
    public String getIndexPage(@PathVariable String languageCode,
                           @RequestParam(value = "parentCode", required = false) String parentCode,
                           @RequestParam(value = "messageType", required = false) MessageType messageType,
                           Model model) {

        log.info("Request to show pages list for parent code : {}", parentCode);
        pageService.getIndexPage(parentCode, Language.getLanguage(languageCode), messageType, model);
        return "admin/index";
    }

    @GetMapping(value = LANGUAGE_PREFIX + ADMIN_PREFIX + "/pages/create")
    public String getCreatePage(@PathVariable String languageCode,
                               @RequestParam(value = "parentCode", required = false) String parentCode,
                               @ModelAttribute("page") Page page,
                               Model model) {

        log.info("Request to get create en page");
        Language language = Language.getLanguage(languageCode);
        pageService.getCreatePage(language, parentCode, model);
        model.addAttribute("langPrefix", "/" + language.name().toLowerCase(Locale.ROOT));
        return "admin/create";
    }

    @PostMapping(value = LANGUAGE_PREFIX + ADMIN_PREFIX + "/pages")
    public String savePage(@PathVariable String languageCode,
                           @RequestParam(value = "parentCode", required = false) String parentCode,
                           @ModelAttribute("page") Page page) {

        log.info("Request to save page : {}", page);
        page.setParentPage(Page.builder().code(parentCode).build());
        pageService.save(page);
        String code = "/" + languageCode;
        return "redirect:" + code + "/admin/pages?messageType=SAVED&parentCode=" + page.getParentPage().getCode();
    }

    @GetMapping(value = LANGUAGE_PREFIX + ADMIN_PREFIX + "/pages/{pageCode}")
    public String getPageForAdmin(@PathVariable String languageCode,
                                  @PathVariable String pageCode, Model model) {
        log.info("Request to show page with code : {}", pageCode);

        return getPage(languageCode, pageCode, model);
    }

    @GetMapping(value = LANGUAGE_PREFIX + "/{pageCode}")
    public String getPage(@PathVariable String languageCode,
                          @PathVariable String pageCode, Model model) {
        log.info("handling request with pageCode: {}", pageCode);
        String purePageCode = pageService.purifyPageCode(pageCode);
        if (!purePageCode.equals(pageCode)) {
            log.info("redirecting from {} to {}", pageCode, purePageCode);
            return getPage(languageCode, purePageCode, model);
        }
        pageService.render(Language.getLanguage(languageCode), pageCode, model);
        return "base_template";
    }

    @GetMapping(value = LANGUAGE_PREFIX + ADMIN_PREFIX + "/pages/{pageCode}/edit")
    public String getEditPage(@PathVariable String languageCode,
                             @PathVariable String pageCode,
                             Model model) {

        log.info("Request to get edit page for code : {}", pageCode);
        Language language = Language.getLanguage(languageCode);
        pageService.getEditPage(language, pageCode, model);
        model.addAttribute("langPrefix", "/" + language.name().toLowerCase(Locale.ROOT));
        return "admin/edit";
    }

    @PutMapping(value = LANGUAGE_PREFIX + ADMIN_PREFIX + "/pages/{pageCode}")
    public String updatePage(@PathVariable String languageCode,
                               @ModelAttribute("page") Page page) {
        log.info("Request to update page : {}", page);
        pageService.update(page);
        String code = "/" + languageCode;
        return "redirect:" + code + "/admin/pages?messageType=UPDATED&parentCode=" + page.getParentPage().getCode();
    }

    @DeleteMapping(value = LANGUAGE_PREFIX + ADMIN_PREFIX + "/pages/{pageCode}")
    public String destroyAction(@PathVariable String languageCode,
                                @PathVariable String pageCode) {
        log.debug("Request to delete page with code : {}", pageCode);
        Page page = pageService.delete(pageCode);
        String code = "/" + languageCode;
        return "redirect:" + code + "/admin/pages?messageType=DELETED&parentCode=" + page.getParentPage().getCode();
    }

    @GetMapping(value = LANGUAGE_PREFIX)
    public String getRootPage(@PathVariable String languageCode,
                              Model model) {
        log.info("handling root page request");
        return getPage(languageCode, "root", model);
    }

    @GetMapping(value = "favicon.ico")
    @ResponseBody
    void returnNoFavicon() {
    }
}
