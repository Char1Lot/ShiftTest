package ru.char1lot;

import org.apache.commons.cli.*;
import java.util.List;

/*
 *
 * Класс обрабатывает командную строку, нужен для парсинга флагов и аргументов пользователя
 *
 * */

public class CmdHandler {

    private Options options;
    private CommandLineParser parser;
    private HelpFormatter formatter;

    public CmdHandler(){
        options = new Options();
        parser = new DefaultParser();
        formatter = new HelpFormatter();

        Option output = new Option("o", "output",true,"output files path");
        output.setRequired(false);
        options.addOption(output);

        Option prefix = new Option("p", "prefix", true, "prefix for output files");
        prefix.setRequired(false);
        options.addOption(prefix);

        Option help = new Option("h", "help",false,"help with arguments ");
        help.setRequired(false);
        options.addOption(help);

        Option add = new Option("a", "append", false, "append new data in existing files, need to use same prefix if u already used this util, else creates new files with new prefix");
        add.setRequired(false);
        options.addOption(add);

        Option shortStat = new Option("s", "short", false, "given short statistic of all files, try it and u will understand");
        shortStat.setRequired(false);
        options.addOption(shortStat);

        Option fullStat = new Option("f","full", false, "given full statistic of all files, try it and u wil understand");
        fullStat.setRequired(false);
        options.addOption(fullStat);
    }

    /*
     * Главный метод, принимает то что написал пользователь в командной строке
     * разбивает пользовательский ввод на флаги, аргументы этих флагов
     * */

    public CommandLine parse(String[] args) throws ParseException {
        CommandLine cmd = parser.parse(options, args, true);

        if(cmd.hasOption("h")){
            formatter.printHelp("File filter util (created by Elistratov Denis)", options);
            System.exit(0);
        }
        return cmd;
    }

    public String getOutPath(CommandLine cmd){
        return cmd.getOptionValue("output", "");
    }

    public String getPrefix(CommandLine cmd){
        return cmd.getOptionValue("prefix", "");
    }

    public boolean isNeedRewrite(CommandLine cmd){
        return !cmd.hasOption("a");
    }

    public List<String> getFileNames(CommandLine cmd){
        return cmd.getArgList();
    }

    public boolean shortStatIsNeeded(CommandLine cmd){
        return cmd.hasOption("s");
    }

    public boolean fullStatIsNeeded(CommandLine cmd){
        return cmd.hasOption("f");
    }

}
