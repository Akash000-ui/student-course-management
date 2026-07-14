import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Category {
    id: string;
    name: string;
    description?: string;
    iconUrl?: string;
    active: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface CategoryRequest {
    name: string;
    description?: string;
    iconUrl?: string;
}

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
    statusCode: number;
}

@Injectable({
    providedIn: 'root'
})
export class CategoryService {
    private baseUrl = 'http://localhost:8080/api/categories';

    constructor(private http: HttpClient) { }

    /**
     * Get all active categories (public)
     */
    getAllCategories(): Observable<ApiResponse<Category[]>> {
        return this.http.get<ApiResponse<Category[]>>(this.baseUrl);
    }

    /**
     * Get all categories including inactive ones (admin only)
     */
    getAllCategoriesForAdmin(): Observable<ApiResponse<Category[]>> {
        return this.http.get<ApiResponse<Category[]>>(`${this.baseUrl}/admin/all`);
    }

    /**
     * Get category by ID
     */
    getCategoryById(id: string): Observable<ApiResponse<Category>> {
        return this.http.get<ApiResponse<Category>>(`${this.baseUrl}/${id}`);
    }

    /**
     * Create a new category (admin only)
     */
    createCategory(request: CategoryRequest): Observable<ApiResponse<Category>> {
        return this.http.post<ApiResponse<Category>>(this.baseUrl, request);
    }

    /**
     * Update a category (admin only)
     */
    updateCategory(id: string, request: CategoryRequest): Observable<ApiResponse<Category>> {
        return this.http.put<ApiResponse<Category>>(`${this.baseUrl}/${id}`, request);
    }

    /**
     * Delete (soft delete) a category (admin only)
     */
    deleteCategory(id: string): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
    }

    /**
     * Permanently delete a category (admin only)
     */
    permanentlyDeleteCategory(id: string): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}/permanent`);
    }
}
