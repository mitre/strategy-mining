package org.mitre.emd;

import ec.util.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import ec.*;

import org.nlogo.core.LogoList;

public class RadarProblem extends GeneralProblem {

    @Override
    public String[] metricNames() {
        return new String[] { "metric" };
    }

}
