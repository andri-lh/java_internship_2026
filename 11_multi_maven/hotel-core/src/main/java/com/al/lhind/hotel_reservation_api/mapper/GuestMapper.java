package com.al.lhind.hotel_reservation_api.mapper;

import com.al.lhind.hotel_reservation_api.dto.GuestDTO;
import com.al.lhind.hotel_reservation_api.model.entity.Guest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring") //Experimented with mapstruct
public interface GuestMapper {

    GuestDTO toDTO(Guest guest);

    Guest toEntity(GuestDTO dto);
}
