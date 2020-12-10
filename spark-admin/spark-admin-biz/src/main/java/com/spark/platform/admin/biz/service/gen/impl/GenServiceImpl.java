package com.spark.platform.admin.biz.service.gen.impl;

import cn.hutool.core.util.ZipUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.FileOutConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.spark.platform.admin.api.entity.gen.TableColumnInfo;
import com.spark.platform.admin.api.vo.GeneratorConfigVo;
import com.spark.platform.admin.biz.dao.gen.GenDao;
import com.spark.platform.admin.biz.service.gen.GenService;
import com.spark.platform.common.config.datasource.DataSourceProperties;
import com.spark.platform.common.config.properties.SparkProperties;
import com.spark.platform.common.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName: spark-platform
 * @Package: com.spark.platform.adminbiz.service.gen.impl
 * @ClassName: GenServiceImpl
 * @Author: wangdingfeng
 * @Description: 自动生成配置
 * @Date: 2020/4/15 11:55
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GenServiceImpl implements GenService {

    private final GenDao genDao;
    private final DataSourceProperties dataSourceProperties;
    private final SparkProperties sparkProperties;

    @Override
    public IPage tableInfoPage(Page page, String tableName) {
        return genDao.tableInfoPage(page,dataSourceProperties.getName(), tableName);
    }

    @Override
    public List<TableColumnInfo> findTableColumnInfo(String tableName) {
        return genDao.findTableColumnInfo(dataSourceProperties.getName(), tableName);
    }

    @Override
    public String generatorCode(GeneratorConfigVo generatorConfigVo) {
        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();
        mpg.setTemplateEngine(new FreemarkerTemplateEngine());
        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = sparkProperties.getFilePath() + File.separator + "codes" + File.separator + generatorConfigVo.getTableName();
        String outputDir = projectPath + "/src/main/java";
        gc.setOutputDir(outputDir);
        //覆盖文件
        gc.setFileOverride(true);
        gc.setAuthor(generatorConfigVo.getAuthor());
        gc.setOpen(true);
        //配置生成dao层和service路径
        gc.setMapperName("%sDao");
        gc.setServiceName("%sService");
        gc.setServiceImplName("%sServiceImpl");
        gc.setControllerName("%sController");
        gc.setEnableCache(false);
        gc.setSwagger2(true);
        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(dataSourceProperties.getUrl());
        dsc.setDriverName(dataSourceProperties.getDriverClassName());
        dsc.setUsername(dataSourceProperties.getUsername());
        dsc.setPassword(dataSourceProperties.getPassword());
        mpg.setDataSource(dsc);

        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setModuleName(generatorConfigVo.getModelName());
        pc.setParent(generatorConfigVo.getParentPackage());
        pc.setMapper("dao");
        mpg.setPackageInfo(pc);

        gc.setBaseColumnList(true);
        gc.setBaseResultMap(true);
        gc.setEnableCache(false);

        // 如果模板引擎是 freemarker
        String templatePath = "/templates/mapper.xml.ftl";
        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };
        // 自定义输出配置
        List<FileOutConfig> focList = new ArrayList<>();
        // 自定义配置会被优先输出
        focList.add(new FileOutConfig(templatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
                return projectPath + "/src/main/resources/mapper/" + pc.getModuleName()
                        + "/" + tableInfo.getEntityName() + "Dao" + StringPool.DOT_XML;
            }
        });
        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setController("/templates/controller.java");
        templateConfig.setXml(null);
        mpg.setTemplate(templateConfig);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setSuperEntityClass("com.spark.platform.common.base.entity.BaseEntity");
        strategy.setEntityLombokModel(true);
        strategy.setRestControllerStyle(true);
        // 公共父类
        strategy.setSuperControllerClass("com.spark.platform.common.base.support.BaseController");
        // 写于父类中的公共字段
        strategy.setSuperEntityColumns("creator", "modifier", "create_date", "modify_date", "del_flag", "remarks");
        strategy.setInclude(generatorConfigVo.getTableName());
        strategy.setControllerMappingHyphenStyle(true);
        strategy.setTablePrefix(pc.getModuleName() + "_");
        mpg.setStrategy(strategy);
        mpg.setTemplateEngine(new FreemarkerTemplateEngine());
        mpg.execute();
        return projectPath;
    }

    @Override
    public void downLoadCodes(GeneratorConfigVo generatorConfigVo, HttpServletResponse response) {
        try {
            String zipFolderPath = generatorCode(generatorConfigVo);
            File zipFile = ZipUtil.zip(zipFolderPath);
            String fileName = generatorConfigVo.getTableName() + ".zip";
            FileUtil.download(fileName, new FileInputStream(zipFile), response);
        } catch (IOException e) {
            log.error("下载失败", e);
        }
    }
}
