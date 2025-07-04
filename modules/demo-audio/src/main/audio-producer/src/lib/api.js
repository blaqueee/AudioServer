import axios from 'axios';
import {CUSTOMS_OFFICE_BASE_URL} from '@/config.js';

export const fetchParents = async () => {
  try {
    const response = await axios.get(`${CUSTOMS_OFFICE_BASE_URL}/parents`);
    return response.data.data;
  } catch (error) {
    console.error('Ошибка при получении корневых таможенных офисов:', error);
    return [];
  }
};

export const fetchChildren = async (parentId) => {
  try {
    const response = await axios.get(`${CUSTOMS_OFFICE_BASE_URL}/by/parent`, {
      params: { parentId }
    });
    return response.data.data;
  } catch (error) {
    console.error(`Ошибка при получении дочерних офисов для parentId ${parentId}:`, error);
    return [];
  }
};