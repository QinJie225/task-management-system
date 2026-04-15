import { z } from "zod";

export const createTaskSchema = z.object({
  title: z.string()
    .min(3, "Title must be at least 3 characters")
    .max(35, "Title must be at most 35 characters"),
  description: z.string()
    .min(3, "Description must be at least 3 characters")
    .max(100, "Description must be at most 100 characters"),
  priority: z.enum(["LOW", "MEDIUM", "HIGH"], {
    message: "Priority is required"
  }),
  dueDate: z.string()
    .min(1, "Due date is required")
    .refine((date) => new Date(date) >= new Date(new Date().setHours(0,0,0,0)), {
      message: "Due date must be today or in the future"
    }),
});


export const updateTaskSchema = z.object({
  title: z.string()
    .min(3, "Title must be at least 3 characters")
    .max(35, "Title must be at most 35 characters")
    .optional(),
  description: z.string()
    .min(3, "Description must be at least 3 characters")
    .max(100, "Description must be at most 100 characters")
    .optional(),
  priority: z.enum(["LOW", "MEDIUM", "HIGH"], { message: "Invalid priority" }).optional(),
  dueDate: z.string()
    .refine((date) => new Date(date) >= new Date(new Date().setHours(0,0,0,0)), {
      message: "Due date must be today or in the future"
    })
    .optional(),
}).partial();