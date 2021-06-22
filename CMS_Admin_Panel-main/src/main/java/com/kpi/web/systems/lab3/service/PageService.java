package com.kpi.web.systems.lab3.service;

import com.kpi.web.systems.lab3.entity.Page;
import com.kpi.web.systems.lab3.entity.enums.Language;
import com.kpi.web.systems.lab3.entity.enums.MessageType;
import org.springframework.ui.Model;

import java.util.List;

public interface PageService {

    List<Page> findChildPages(String parentCode);

    void save(Page page);

    void update(Page page);

    Page delete(String page);

    void getIndexPage(String parentCode, Language language, MessageType messageType, Model model);

    void getCreatePage(Language language, String parentCode, Model model);

    void getEditPage(Language language, String pageCode, Model model);

    Page findPageByCode(String pageCode);

    String purifyPageCode(String pageCode);

    Model render(Language language, String pageCode, Model model);
}
