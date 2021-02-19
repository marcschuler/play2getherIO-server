package de.karlthebee.commongames.rest;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/statistics")
@CrossOrigin
@Data
@Slf4j
public class StatisticRest {
}
