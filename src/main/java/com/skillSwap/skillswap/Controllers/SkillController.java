package com.skillSwap.skillswap.Controllers;

import com.skillSwap.skillswap.Entity.Skill;
import com.skillSwap.skillswap.Service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/skillswap/v1/skill")
public class SkillController {

    @Autowired
    private SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping("/add")
    public Skill addSkill(@RequestBody Skill skill){
        return skillService.addSkill(skill);
    }

    @GetMapping("/getskill")
    public List<Skill> getAllSkill(){
        return skillService.getAllSkills();
    }

    @GetMapping("/getskill/{id}")
    public Skill getSkillById(@PathVariable int id){
        return skillService.getSkillById(id);
    }
}
