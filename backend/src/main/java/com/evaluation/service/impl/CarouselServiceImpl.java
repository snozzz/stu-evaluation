package com.evaluation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluation.entity.Carousel;
import com.evaluation.mapper.CarouselMapper;
import com.evaluation.service.CarouselService;
import org.springframework.stereotype.Service;

@Service
public class CarouselServiceImpl extends ServiceImpl<CarouselMapper, Carousel> implements CarouselService {
}
